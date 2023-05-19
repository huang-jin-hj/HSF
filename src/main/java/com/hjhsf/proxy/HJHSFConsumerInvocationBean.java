package com.hjhsf.proxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hjhsf.HJHSFCommunication;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * Created by huangJin on 2023/5/17.
 */
public class HJHSFConsumerInvocationBean implements InvocationHandler, FactoryBean {
    private Class<?> hsfInterface;

    private String serviceName;

    @Autowired
    private HJHSFCommunication hjhsfCommunication;

    public HJHSFConsumerInvocationBean(Class<?> hsfInterface, String serviceName) {
        this.hsfInterface = hsfInterface;
        this.serviceName = serviceName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        Set<String> metaInfo = this.hjhsfCommunication.getMetaInfo(this.serviceName);
        //永远获取第一个 先不加负载均衡
        String next = metaInfo.iterator().next();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("className", buildDefaultBeanName(className));
        jsonObject.put("methodName", methodName);
        if (args != null){
            JSONObject jsonObjectParameters = new JSONObject();
            for (int i = 0; i < args.length; i++) {
                jsonObjectParameters.put("parameter_" + i, args[i]);
            }
            jsonObject.put("parameters", jsonObjectParameters);
        }
        Class<?> returnType = method.getReturnType();
        String s = hjhsfCommunication.remoteCall(next, JSONObject.toJSONString(jsonObject));
        return JSON.parseObject(s, returnType);
    }

    @Override
    public Object getObject() throws Exception {
        return Proxy.newProxyInstance(hsfInterface.getClassLoader(), new Class[]{hsfInterface}, this);
    }

    @Override
    public Class<?> getObjectType() {
        return this.hsfInterface;
    }


    private static String buildDefaultBeanName(String className) {
        String shortClassName = ClassUtils.getShortName(className);
        return Introspector.decapitalize(shortClassName);
    }

    public static void main(String[] args) {
    }
}
