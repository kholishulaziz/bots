package com.springboot.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

@Configuration
@EnableScheduling
public class Bots {

    private final static Logger LOG = LoggerFactory.getLogger(Bots.class);

    @Value("${url.bots}")
    private String urlBots;

    @Value("${url.search}")
    private String urlSearch;

    @Value("${query.search}")
    private String[] querySearch;

    @Value("${url.list}")
    private String[] urlList;

    @Value("${user.agent}")
    private String userAgent;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        poll(urlBots);
        for (String url : querySearch) {
            poll(urlSearch + url);
        }
        for (String url : urlList) {
            poll(url);
        }
        LOG.info("~");
    }

    @Scheduled(cron = "${scheduled.cron.expression}")
    private void cronScheduled() {
        poll(urlBots);
    }

    @Scheduled(fixedRateString = "${scheduled.fixedRate.in.milliseconds}")
    private void fixedRateScheduled() {
        Random rand = new Random();
        String url;
        // optional poll
        if (rand.nextBoolean()) {
            url = urlSearch + querySearch[rand.nextInt(querySearch.length)];
            poll(url);
        }
        // always poll
        url = urlList[rand.nextInt(urlList.length)];
        poll(url);
    }

    private void poll(String url) {
        try {
            URL urlObj = new URL(url);
            // create http request
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", userAgent);
            con.setConnectTimeout(20000);
            con.connect();
            String responseMessage = con.getResponseMessage();
            if(responseMessage.equals("OK"))
                LOG.info("{}", responseMessage);
            else
                LOG.error("{}: {}...", con.getResponseCode(), url.substring(0,20));
            con.disconnect();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

}
