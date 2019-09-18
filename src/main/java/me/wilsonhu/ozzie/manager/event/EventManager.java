package me.wilsonhu.ozzie.manager.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager
{
    private static Map<Class<? extends Event>, List<EventListener>> mappedEventListeners =
            new HashMap<Class<? extends Event>, List<EventListener>>();

    public static void registerEvents(Object listener)
    {
        for (Method method : listener.getClass().getMethods())
        {      
            if(method.isAnnotationPresent(EventAnnotation.class)) {
            	EventAnnotation annotation = method.getAnnotation(EventAnnotation.class);
            	if(annotation != null)
                {
                    Class<? extends Event> eventClass = annotation.event();
                    List<EventListener> listeners = mappedEventListeners.get(eventClass);
                    if(listeners == null)
                    {
                        listeners = new ArrayList<EventListener>();
                    }
                    listeners.add(new EventListener(method, listener));
                    mappedEventListeners.put(eventClass, listeners);
                }
            }   
        }
    }
    
    public static void fireEvent(Event event)
    {
        Class<? extends Event> eventClass = event.getClass();
        List<EventListener> listeners = mappedEventListeners.get(eventClass);
        if(listeners != null)
        {
            for(EventListener listener : listeners)
            {
                Method method = listener.getMethod();
                Object clazz = listener.getListener();
                try
                {
                    method.invoke(clazz);
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
                catch (InvocationTargetException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}