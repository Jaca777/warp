package net.warpgame.engine.core.context.config;

import net.warpgame.engine.core.context.service.Service;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Jaca777
 * Created 2017-09-23 at 15
 */
@Service
public class Config {

    private ConfigurationManager configurationManager;
    private Map<String, Object> values = new TreeMap<>();

    public Config(ConfigurationManager configurationManager, ConfigLoader configLoader) {
        this.configurationManager = configurationManager;
        configLoader.loadTo(this);
    }

    public void setValue(String name, Object value) {
        values.put(name, value);
        configurationManager.updateValue(name, value);
    }

    public <T> T getValue(String name) {
        return (T) values.get(name);
    }
}