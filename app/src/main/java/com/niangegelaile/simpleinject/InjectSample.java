package com.niangegelaile.simpleinject;

import android.util.Log;

import com.niangegelaile.injectannotation.AutoInject;
import com.niangegelaile.injectannotation.InjectUtil;

public class InjectSample {
    @AutoInject
    String name;

    public InjectSample() {
        InjectUtil.inject(new InjectComponent(),this);
        Log.e("InjectSample",name);
    }

}
