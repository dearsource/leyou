package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.Exception.LyException;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.*;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private ItemClient itemClient;
    @Autowired
    private ElasticsearchTemplate esTemplate;

    public Goods createGoods(SpuDTO spuDTO){
        Long spuId = spuDTO.getId();
//        BrandDTO brandDTO = itemClient.findById(spuDTO.getBrandId());
        String categoryName = spuDTO.getCategoryName();
        String spuName = spuDTO.getName();
        String all = categoryName + spuDTO.getBrandName()+spuName;
        //从item中获取skus
        List<Map<String,Object>> skusMapList = new ArrayList<>();
        List<SkuDTO> skuDTOList = itemClient.findSkuListBySpuId(spuId);
        for(SkuDTO skuDTO:skuDTOList){
            Map<String,Object> map = new HashMap<>();
            map.put("id",skuDTO.getId());
            map.put("title",skuDTO.getTitle());
            map.put("image", StringUtils.substringBefore(skuDTO.getImages(),","));
            map.put("price",skuDTO.getPrice());
            skusMapList.add(map);
        }
        //构造price 的集合
        Set<Long> price = skuDTOList.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());
        //获取规格参数的名字的集合
        List<SpecParamDTO> specParamList = itemClient.findParamList(null, spuDTO.getCid3(), true);
        //获取spudetal的值
        SpuDetailDTO spuDetailDTO = itemClient.findSpuDetail(spuId);
        //获取通用规格参数
        String genericSpec = spuDetailDTO.getGenericSpec();
        //构造通过规格参数的map key - 参数的id ，value -- 规格的值
        Map<Long, Object> genericMap = JsonUtils.toMap(genericSpec, Long.class, Object.class);
        //获取特殊规格参数
        String specialSpec = spuDetailDTO.getSpecialSpec();
        Map<Long, List<Object>> specialMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<Object>>>() {
        });

        //规格参数名字和值的map  key  -规格参数的名字  value -值
        Map<String,Object> specs = new HashMap<>();
        for(SpecParamDTO paramDTO:specParamList){

            Long paramId = paramDTO.getId();
            String key = paramDTO.getName();
            Object value = null;
            //判断是否 通用
            if(paramDTO.getGeneric()){
                value = genericMap.get(paramId);
            }else{
                value = specialMap.get(paramId);
            }
            if(paramDTO.getIsNumeric()){
                value = chooseSegment(value,paramDTO);
            }
            specs.put(key,value);

        }
        Goods goods = new Goods();
        goods.setAll(all);
        goods.setSkus(JsonUtils.toString(skusMapList));
        goods.setPrice(price);
        goods.setSpecs(specs);
        goods.setSubTitle(spuDTO.getSubTitle());
        goods.setCreateTime(spuDTO.getCreateTime().getTime());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setBrandId(spuDTO.getBrandId());
        goods.setId(spuId);

        return goods;
    }


    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 用户输入 关键字，进行搜索
     * @param request
     */
    public PageResult<GoodsDTO> search(SearchRequest request){

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //过滤返回的列
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //构造查询的条件
//        queryBuilder.withQuery(QueryBuilders.matchQuery("all",key).operator(Operator.AND));
        queryBuilder.withQuery(basicQuery(request));
//构造翻页的信息
        int page  = request.getPage() -1;
        queryBuilder.withPageable(PageRequest.of(page,10));
//        进行查询
        AggregatedPage<Goods> aggregatedPage = esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        //总条数
        long total = aggregatedPage.getTotalElements();
        //总页数
        int totalPages = aggregatedPage.getTotalPages();
        List<Goods> goodsList = aggregatedPage.getContent();
        if(CollectionUtils.isEmpty(goodsList)){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        List<GoodsDTO> goodsDTOS = BeanHelper.copyWithCollection(goodsList, GoodsDTO.class);
        return  new PageResult<GoodsDTO>(total,Long.valueOf(String.valueOf(totalPages)),goodsDTOS);
    }

    /**
     * 查询过滤的项
     */
    public Map<String,List<?>> getFilter(SearchRequest request){
        Map<String,List<?>> filterMap = new LinkedHashMap<>();
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //过滤返回的列
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());
        //构造查询的条件
//        queryBuilder.withQuery(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
        queryBuilder.withQuery(basicQuery(request));
        queryBuilder.withPageable(PageRequest.of(0,1));

        //处理聚合操作
        String categoryAgg = "categroyAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAgg).field("categoryId"));
        String  brandAgg = "brangAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAgg).field("brandId"));
//        进行查询
        AggregatedPage<Goods> result = esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        Aggregations aggregations = result.getAggregations();

        LongTerms categoryTerms = aggregations.get(categoryAgg);
        List<CategoryDTO> categoryDTOList = handlerCategory(categoryTerms,filterMap);
        LongTerms brandTerms = aggregations.get(brandAgg);
        handlerBrand(brandTerms,filterMap);
        //只有当分类的返回值是一个的时候，才去聚合规格参数
        if(!CollectionUtils.isEmpty(categoryDTOList) && categoryDTOList.size()==1){
            //规格参数的过滤条件获取
            handlerSpec(request,filterMap,categoryDTOList.get(0).getId());
        }

        return filterMap;
    }

    /**
     * 操作规格参数的 过滤条件
     * @param request
     * @param filterMap
     */
    private void handlerSpec(SearchRequest request, Map<String, List<?>> filterMap,Long cid) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //过滤返回的列
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());
        //构造查询的条件
//        queryBuilder.withQuery(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
        queryBuilder.withQuery(basicQuery(request));

        queryBuilder.withPageable(PageRequest.of(0,1));
        //获取规格参数的名字
        List<SpecParamDTO> paramList = itemClient.findParamList(null, cid, true);
        for (SpecParamDTO paramDTO : paramList) {
            //规格参数的名字
            String specName = paramDTO.getName();
//            es中要进行聚合的 列的名字
            String fieldName = "specs."+specName;
            queryBuilder.addAggregation(AggregationBuilders.terms(specName).field(fieldName));
        }
        AggregatedPage<Goods> result = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = result.getAggregations();
        for (SpecParamDTO paramDTO : paramList) {
            //规格参数的名字
            String specName = paramDTO.getName();
            StringTerms stringTerms = aggregations.get(specName);
            //获取聚合的桶
            List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
//            List<String> aggList = new ArrayList<>();
//            for (StringTerms.Bucket bucket : buckets) {
//                String aggValue = bucket.getKeyAsString();
//                aggList.add(aggValue);
//            }
            List<String> aggList = buckets.stream().map(StringTerms.Bucket::getKeyAsString).collect(Collectors.toList());
            filterMap.put(specName,aggList);
        }

    }

    /**
     * 基本查询
     * 包含关键词 match查询
     * 和过滤条件
     * @param request
     * @return
     */
    public QueryBuilder basicQuery(SearchRequest request){
//        构造bool查询 must  filter
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //构造must条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
//      key - 分类、品牌、规格参数的名字  value - 用户选择过滤条件
        Map<String, String> filterMap = request.getFilterMap();

        for (String key : filterMap.keySet()) {
            String value = filterMap.get(key);
            //es中的 列名字
            String filterName = "specs."+key;
            if(key.equals("分类")){
                filterName = "categoryId";
            }else if(key.equals("品牌")){
                filterName = "brandId";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(filterName,value));
        }
        return boolQueryBuilder;
    }

    private void handlerBrand(LongTerms brandTerms, Map<String, List<?>> filterMap) {
        List<LongTerms.Bucket> buckets = brandTerms.getBuckets();

        List<Long> brandIds = buckets.stream().map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        List<BrandDTO> brandDTOList = itemClient.findBrandListByIds(brandIds);
        filterMap.put("品牌",brandDTOList);
    }

    private List<CategoryDTO> handlerCategory(LongTerms categoryTerms, Map<String, List<?>> filterMap) {
        List<LongTerms.Bucket> buckets = categoryTerms.getBuckets();

        List<Long> cids = new ArrayList<>();
        for (LongTerms.Bucket bucket : buckets) {
            long categoryId = bucket.getKeyAsNumber().longValue();
            cids.add(categoryId);
        }
        List<CategoryDTO> categoryDTOList = itemClient.findCateogrySByCids(cids);
        filterMap.put("分类",categoryDTOList);
        return categoryDTOList;
    }
}
