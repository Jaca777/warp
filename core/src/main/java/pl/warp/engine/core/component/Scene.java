package pl.warp.engine.core.component;


import pl.warp.engine.core.context.EngineContext;
import pl.warp.engine.core.component.listenable.SimpleListenableParent;

/**
 * @author Jaca777
 *         Created 2016-06-25 at 16
 */
public class Scene extends SimpleListenableParent {
    public Scene(EngineContext context) {
        super(context);
    }
}
