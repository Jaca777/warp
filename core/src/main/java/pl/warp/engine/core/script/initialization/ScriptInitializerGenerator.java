package pl.warp.engine.core.script.initialization;

import pl.warp.engine.core.script.Script;

import java.util.function.Consumer;

/**
 * @author Jaca777
 * Created 2017-09-08 at 22
 */
public interface ScriptInitializerGenerator {
    Consumer<? extends Script> getInitializer(Class<? extends Script> scriptClass);

}