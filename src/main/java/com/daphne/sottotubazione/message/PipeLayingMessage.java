package com.geowebframework.pipeLaying.message;

import it.eagleprojects.gisfocommons.messages.GisfoStringMessages;
import org.springframework.stereotype.Component;

@Component
public class PipeLayingMessage extends GisfoStringMessages {

    @Override
    protected String getBundlesFolderName() {
        return "pipe-laying";
    }
}