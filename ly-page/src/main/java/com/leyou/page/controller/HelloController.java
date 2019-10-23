package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
public class HelloController {

    @Autowired
    private PageService pageService;
    @RequestMapping("/hello")
    public String hello(Model model){
        model.addAttribute("name","jack");
        model.addAttribute("age",null);
        return "hello";
    }

    @RequestMapping("/item/{id}.html")
    public String hello(Model model, @PathVariable(name="id") Long spuId){
        Map<String, Object> map = pageService.loadData(spuId);
        model.addAllAttributes(map);
        return "item";
    }
}
