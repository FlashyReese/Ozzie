package me.wilsonhu.ozzie.manager.event;

import java.lang.reflect.Method;

public class EventListener
{
    private final Method method;
    private final Object listener;

    public EventListener(Method method, Object listener)
    {
        this.method = method;
        this.listener = listener;
    }

    public Method getMethod()
    {
        return method;
    }

    public Object getListener()
    {
        return listener;
    }
}