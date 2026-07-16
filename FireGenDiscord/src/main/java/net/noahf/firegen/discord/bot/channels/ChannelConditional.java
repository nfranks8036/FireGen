package net.noahf.firegen.discord.bot.channels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.utilities.StringSelectors;
import net.noahf.firegen.discord.utilities.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @RequiredArgsConstructor
public class ChannelConditional {

    private final List<Class<? extends StringSelectors>> clazzPath;
    private final List<String> matchString;


    private List<Method> methods = new ArrayList<>();

    public boolean evaluate(Incident incident) {
        Object currentObject;
        try {
            if (this.methods.isEmpty()) {
                currentObject = this.findMethods(incident);
            } else {
                currentObject = incident;
                for (Method method : methods) {
                    if (currentObject == null) {
                        return false;
                    }

                    try {
                        currentObject = method.invoke(currentObject);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (currentObject == null) {
                return false;
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException("An error occurred while traversing Incident: " + exception, exception);
        }

        if (!(currentObject instanceof StringSelectors selector)) {
            throw new IllegalArgumentException("Ending object '" + clazzPath.getLast().getCanonicalName() + "' must extend or implement " + StringSelectors.class.getSimpleName() + ", got " + currentObject);
        }

        for (String string : matchString) {
            if (selector.asStringSelectors().contains(string)) {
                return true;
            }
        }

        return false;
    }



    private Object findMethods(Incident incident) throws InvocationTargetException, IllegalAccessException {
        List<Method> returned = new ArrayList<>();
        StringSelectors currentObject = incident;
        Class<? extends StringSelectors> current = Incident.class;
        for (Class<? extends StringSelectors> clazz : clazzPath) {
            Method method = this.findFirstMethodWithReturnType(current.getMethods(), clazz);
            if (method == null) {
                throw new IllegalArgumentException("Argument '" + clazz.getCanonicalName() + "' is not a zero-parameter return type in the '" + current.getCanonicalName() + "' class.");
            }

            returned.add(method);

            Object obj = method.invoke(currentObject);
            if (!(obj instanceof StringSelectors selector)) {
                return null;
            }
            currentObject = selector;
            current = currentObject.getClass();
        }
        this.methods = returned;
        return currentObject;
    }

    private Method findFirstMethodWithReturnType(Method[] methods, Class<?> returnType) {
        for (Method method : methods) {
            if (method.getParameterCount() > 0) {
                continue;
            }

            if (method.getReturnType().equals(returnType)) {
                return method;
            }
        }
        return null;
    }

}
