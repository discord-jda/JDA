module net.dv8tion.JDA {
    requires jsr305;
    requires java.desktop;
    
    exports net.dv8tion.jda.bot;
    exports net.dv8tion.jda.bot.entities;
    exports net.dv8tion.jda.bot.sharding;
    exports net.dv8tion.jda.bot.utils.cache;
    
    exports net.dv8tion.jda.client;
    exports net.dv8tion.jda.client.entities;
    exports net.dv8tion.jda.client.events.call.update;
    exports net.dv8tion.jda.client.events.call.voice;
    exports net.dv8tion.jda.client.events.group;
    exports net.dv8tion.jda.client.events.group.update;
    exports net.dv8tion.jda.client.events.message.group;
    exports net.dv8tion.jda.client.events.message.group.react;
    exports net.dv8tion.jda.client.events.relationship;
    exports net.dv8tion.jda.client.exceptions;
    exports net.dv8tion.jda.client.handle;
    exports net.dv8tion.jda.client.managers;
    exports net.dv8tion.jda.client.managers.fields;
    exports net.dv8tion.jda.client.requests.restaction;
    exports net.dv8tion.jda.client.requests.restaction.pagination;
    
    exports net.dv8tion.jda.core;
    exports net.dv8tion.jda.core.audio;
    exports net.dv8tion.jda.core.audio.factory;
    exports net.dv8tion.jda.core.audio.hooks;
    exports net.dv8tion.jda.core.audit;
    exports net.dv8tion.jda.core.entities;
    exports net.dv8tion.jda.core.events;
    exports net.dv8tion.jda.core.events.channel.category;
    exports net.dv8tion.jda.core.events.channel.category.update;
    exports net.dv8tion.jda.core.events.channel.priv;
    exports net.dv8tion.jda.core.events.channel.text;
    exports net.dv8tion.jda.core.events.channel.text.update;
    exports net.dv8tion.jda.core.events.channel.voice;
    exports net.dv8tion.jda.core.events.channel.voice.update;
    exports net.dv8tion.jda.core.events.emote;
    exports net.dv8tion.jda.core.events.emote.update;
    exports net.dv8tion.jda.core.events.guild;
    exports net.dv8tion.jda.core.events.guild.member;
    exports net.dv8tion.jda.core.events.guild.update;
    exports net.dv8tion.jda.core.events.guild.voice;
    exports net.dv8tion.jda.core.events.http;
    exports net.dv8tion.jda.core.events.message;
    exports net.dv8tion.jda.core.events.message.guild;
    exports net.dv8tion.jda.core.events.message.guild.react;
    exports net.dv8tion.jda.core.events.message.priv;
    exports net.dv8tion.jda.core.events.message.priv.react;
    exports net.dv8tion.jda.core.events.message.react;
    exports net.dv8tion.jda.core.events.role;
    exports net.dv8tion.jda.core.events.role.update;
    exports net.dv8tion.jda.core.events.self;
    exports net.dv8tion.jda.core.events.user;
    exports net.dv8tion.jda.core.exceptions;
    exports net.dv8tion.jda.core.handle;
    exports net.dv8tion.jda.core.hooks;
    exports net.dv8tion.jda.core.managers;
    exports net.dv8tion.jda.core.managers.fields;
    exports net.dv8tion.jda.core.requests;
    exports net.dv8tion.jda.core.requests.ratelimit;
    exports net.dv8tion.jda.core.requests.restaction;
    exports net.dv8tion.jda.core.requests.restaction.order;
    exports net.dv8tion.jda.core.requests.restaction.pagination;
    exports net.dv8tion.jda.core.utils;
    exports net.dv8tion.jda.core.utils.cache;
    exports net.dv8tion.jda.core.utils.tuple;
    
    exports net.dv8tion.jda.webhook;
}
