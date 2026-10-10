package com.cloud.demo_blackbaze_b2.controller;


import com.cloud.demo_blackbaze_b2.service.CandidatureService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeControler {

    private final CandidatureService service;

    public HomeControler(CandidatureService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home(Model model){

        model.addAttribute("candidatures", service.lister());


        return "index";
    }
}
