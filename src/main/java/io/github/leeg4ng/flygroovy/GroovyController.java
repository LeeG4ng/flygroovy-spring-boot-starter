package io.github.leeg4ng.flygroovy;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.leeg4ng.flygroovy.auth.AuthPolicy;
import groovy.lang.GroovyClassLoader;
import lombok.SneakyThrows;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Nook Li
 * @date 9/22/2022
 */
//@RestController
//@RequestMapping("/groovy")
public class GroovyController {

    @Autowired
    private AuthPolicy authPolicy;

    @Autowired
    private ApplicationContext applicationContext;

    @SneakyThrows
    @ResponseBody
//    @PostMapping("/exec")
    public String exec(@RequestHeader HttpHeaders httpHeaders,
                       @RequestBody String script,
                       @RequestParam @Nullable String methodName,
                       @RequestParam @Nullable List<String> args) {
        // auth
        if (!authPolicy.auth(httpHeaders)) {
            return "Auth fail";
        }

        Class clazz = new GroovyClassLoader().parseClass(script);

        // 放到容器中装配bean
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();

        AnnotationConfigServletWebServerApplicationContext context = (AnnotationConfigServletWebServerApplicationContext) applicationContext;
        context.getAutowireCapableBeanFactory().applyBeanPostProcessorsAfterInitialization(beanDefinition, "groovyBean");

        String beanName = "groovyBean";
        try {
            context.removeBeanDefinition(beanName);
        } catch (NoSuchBeanDefinitionException ignored) {}

        context.registerBeanDefinition(beanName, beanDefinition);

        Object groovyBean = context.getBean(beanName);

        // 依赖注入后的bean
        clazz = groovyBean.getClass();

        // 尝试找到匹配name和args的方法
        for (Method method : clazz.getDeclaredMethods()) {
            // 比较方法名
            if (!method.getName().equals(Optional.ofNullable(methodName).orElse("run")))
                continue;

            // 比较参数个数
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (args == null || args.size() == 0) {
                if (parameterTypes.length != 0) continue;
            } else {
                if (parameterTypes.length != args.size()) continue;
            }

            // 尝试转换参数
            Object[] params = new Object[parameterTypes.length];
            try {
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    if (parameterType.equals(String.class)) {
                        String stringArg = args.get(i);
                        int length = stringArg.length();
                        if (length >= 2 && stringArg.charAt(0) == '"' && stringArg.charAt(length -1) == '"' ) {
                            stringArg = stringArg.substring(1, length -1);
                        }
                        params[i] = stringArg;
                        continue;
                    }
                    if (CLASS_MAP.containsKey(parameterType)) {
                        parameterType = CLASS_MAP.get(parameterType);
                    }
                    if (parameterType.equals(Character.class)) {
                        if (args.get(i).length() != 1) throw new RuntimeException("Not a Character");
                        params[i] = args.get(i).charAt(0);
                        continue;
                    }
                    Method valueOfMethod = parameterType.getMethod("valueOf", String.class);
                    Object val = valueOfMethod.invoke(null, args.get(i));
                    params[i] = val;
                }
            } catch (Exception e) {
                continue;
            }

            // 找到匹配方法
            Object ret;
            try {
                ret = method.invoke(groovyBean, params);
            } catch (Exception e) {
                // invoke error, return exception stack
                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                return stringWriter.toString();
            }

            try {
                JsonMapper jsonMapper = JsonMapper.builder()
                        .disable(MapperFeature.USE_ANNOTATIONS)
                        .enable(SerializationFeature.INDENT_OUTPUT)
                        .build();

                return ret == null ? "null" : jsonMapper.writeValueAsString(ret);
            } catch (Exception ignored) {
                return ret.toString();
            }
        }
        return String.format("Method \"%s\" Not Found / Parameter Bind Error", methodName);
    }

    private static final Map<Class, Class> CLASS_MAP = new HashMap<Class, Class>() {{
        put(int.class, Integer.class);
        put(long.class, Long.class);
        put(float.class, Float.class);
        put(double.class, Double.class);
        put(boolean.class, Boolean.class);
        put(char.class, Character.class);
        put(byte.class, Byte.class);
        put(short.class, Short.class);
    }};
}
