package de.lingoMetrics;

import de.lingoMetrics.Repository.ReferenzRepository;
import de.lingoMetrics.Repository.WordRepository;
import de.lingoMetrics.Service.*;
import de.lingoMetrics.Service.analysis.TextStatisticsService;
import de.lingoMetrics.Service.analysis.WortSchatzAnalyseService;

import java.io.IOException;
import java.util.List;

public class ApplicationContext {

    private final ServiceManager serviceManager;

    public ApplicationContext() throws IOException {
        WordRepository wordRepository = new WordRepository();
        wordRepository.load();

        this.serviceManager = new ServiceManager(
                new TextStrukturService(),
                List.of(
                        new WortSchatzAnalyseService(wordRepository)::analyze,
                        new TextStatisticsService()::analyze
                ),
                new AuswertungsService(new ReferenzRepository())
        );
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }
}