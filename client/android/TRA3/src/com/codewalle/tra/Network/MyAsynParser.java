package com.codewalle.tra.Network;

import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.parser.AsyncParser;
import org.json.JSONObject;

/**
 * Created by xiangzhipan on 14-9-29.
 */
public class MyAsynParser implements AsyncParser<JSONObject> {
    @Override
    public Future<JSONObject> parse(DataEmitter dataEmitter) {

        return null;
    }

    @Override
    public void write(DataSink dataSink, JSONObject jsonObject, CompletedCallback completedCallback) {

    }
}
