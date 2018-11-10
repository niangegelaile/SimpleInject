package com.niangegelaile.simpleinject;

import com.niangegelaile.injectannotation.InjectProxy;

public class ProxySample implements InjectProxy<InjectSample,InjectComponent> {

    @Override
    public void inject(InjectSample target, InjectComponent component) {
        target.name=component.gettxt();
    }
}
