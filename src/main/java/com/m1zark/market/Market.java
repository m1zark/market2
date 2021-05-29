package com.m1zark.market;

import com.google.inject.Inject;
import com.m1zark.market.Commands.CommandManager;
import com.m1zark.market.Utils.Config.ConfigManager;
import com.m1zark.market.Utils.Config.MessageConfig;
import com.m1zark.market.Utils.Database.DataSource;
import lombok.Getter;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.SynchronousExecutor;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.nio.file.Path;
import java.util.Optional;

@Getter
@Plugin(id = MarketInfo.ID, name = MarketInfo.NAME, version = MarketInfo.VERSION, description = MarketInfo.DESCRIPTION, authors = "m1zark")
public class Market {
    @Inject private Logger logger;
    private static Market instance;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    private ConfigManager config;
    private MessageConfig msgConfig;
    private Cause marketCause;
    private DataSource sql;
    private boolean enabled = true;

    @AsynchronousExecutor private SpongeExecutorService asyncExecutorService;
    @SynchronousExecutor private SpongeExecutorService syncExecutorService;

    @Listener public void onInitialization(GameInitializationEvent e){
        instance = this;
        marketCause = Cause.builder().append(this).build(EventContext.builder().build());

        MarketInfo.startup();
        this.enabled = MarketInfo.dependencyCheck();

        if(this.enabled) {
            this.asyncExecutorService = Sponge.getScheduler().createAsyncExecutor(this);
            this.syncExecutorService = Sponge.getScheduler().createSyncExecutor(this);

            // Setup config options
            this.config = new ConfigManager();
            this.msgConfig = new MessageConfig();

            // Initialize data source and creates tables
            this.sql = new DataSource("Market_Listing","Market_Logs");
            this.sql.createTables();
            getConsole().ifPresent(console -> console.sendMessages(Text.of(MarketInfo.PREFIX, "Initializing database...")));

            // Setup commands
            new CommandManager().registerCommands(this);
            getConsole().ifPresent(console -> console.sendMessages(Text.of(MarketInfo.PREFIX, "Initialization complete!")));
        }
    }

    @Listener(order = Order.POST) public void postGameStart(GameStartedServerEvent event) {

    }

    @Listener public void onReload(GameReloadEvent e) {
        if (this.enabled) {
            this.config = new ConfigManager();
            this.msgConfig = new MessageConfig();
            getConsole().ifPresent(console -> console.sendMessages(Text.of(MarketInfo.PREFIX, "Configurations have been reloaded")));
        }
    }

    @Listener public void onServerStop(GameStoppingServerEvent e) {

    }

    @Listener public void onServerStop(GameStoppingEvent e) {
        try {
            this.sql.shutdown();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static Market getInstance() {
        return instance;
    }

    public Optional<ConsoleSource> getConsole() {
        return Optional.ofNullable(Sponge.isServerAvailable() ? Sponge.getServer().getConsole() : null);
    }

    public Scheduler getScheduler() {
        return Sponge.getScheduler();
    }
}
