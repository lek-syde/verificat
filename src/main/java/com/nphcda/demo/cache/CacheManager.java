package com.nphcda.demo.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

public class CacheManager {

    @Autowired
    private CacheManager cacheManager;      // autowire cache manager
    // clear all cache using cache manager
    @RequestMapping(value = "clearCache")
    public void clearCache(){
       cacheManager.clearCache();
    }
}
