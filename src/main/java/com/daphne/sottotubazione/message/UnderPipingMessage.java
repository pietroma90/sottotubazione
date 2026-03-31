package com.geowebframework.underPiping.message;

import it.eagleprojects.gisfocommons.messages.GisfoStringMessages;
import org.springframework.stereotype.Component;

@Component
public class UnderPipingMessage extends GisfoStringMessages {

    @Override
    protected String getBundlesFolderName() {
        return "under-piping";
    }
}
