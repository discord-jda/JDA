package net.dv8tion.jda.api.interactions.components.replacer;

import net.dv8tion.jda.api.interactions.components.Component;

public interface IReplaceable<C extends IReplaceable<C, E>, E extends Component> extends Component {
    C replace(ComponentReplacer<E> replacer);
}
