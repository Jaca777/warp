package pl.warp.engine.core.context;

import pl.warp.engine.core.context.loader.ContextLoader;
import pl.warp.engine.core.context.loader.JavaContextHolder;
import pl.warp.engine.core.context.loader.service.ServiceInfo;
import scala.Option;

import java.util.List;
import java.util.Optional;

/**
 * @author Jaca777
 * Created 2017-08-29 at 22
 */
public class Context {

    private static final ServiceInfo CONTEXT_SERVICE_INFO = new ServiceInfo(
            Context.class,
            Option.empty(),
            null,
            null
    );

    private JavaContextHolder contextHolder;


    public static Context create() {
        ContextLoader loader = new ContextLoader();
        Context context = new Context();
        JavaContextHolder contextHolder = loader.loadContext(context);
        context.setContextHolder(contextHolder);
        return context;
    }

    public <T> Optional<T> findOne(Class<T> type) {
        return contextHolder.findOne(type, Optional.empty());
    }

    public <T> Optional<T> findOne(Class<T> type, String qualifier) {
        return contextHolder.findOne(type, Optional.of(qualifier));
    }

    public <T> List<T> findAll(Class<T> type) {
        return contextHolder.findAll(type, Optional.empty());
    }

    public <T> List<T> findAll(Class<T> type, String qualifier) {
        return contextHolder.findAll(type, Optional.of(qualifier));
    }

    private void setContextHolder(JavaContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }
}