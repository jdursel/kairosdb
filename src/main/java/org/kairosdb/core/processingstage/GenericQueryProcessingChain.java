package org.kairosdb.core.processingstage;

import com.google.common.collect.ImmutableList;
import org.apache.bval.constraints.NotEmpty;
import org.kairosdb.core.annotation.QueryProcessingStage;
import org.kairosdb.core.processingstage.metadata.QueryProcessingStageMetadata;
import org.kairosdb.core.processingstage.metadata.QueryProcessorMetadata;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public abstract class GenericQueryProcessingChain implements QueryProcessingChain
{
    private List<QueryProcessingStageFactory<?>> processingChain = new ArrayList<>();
    private List<QueryProcessingStageMetadata> queryProcessingStageMetadata = new ArrayList<>();

    protected GenericQueryProcessingChain(@NotNull @NotEmpty List<QueryProcessingStageFactory<?>> processingChain)
    {
        for (int i = 0; i < processingChain.size(); i++)
        {
            QueryProcessingStageFactory<?> factory = processingChain.get(i);
            ArrayList<QueryProcessorMetadata> queryProcessorMetadata = new ArrayList<>();

            QueryProcessingStage annotation = factory.getClass().getAnnotation(QueryProcessingStage.class);
            if (annotation == null)
                throw new IllegalStateException("Processing Stage class " + factory.getClass().getName() +
                        " does not have required annotation " + QueryProcessingStage.class.getName());
            if (factory.getQueryProcessorMetadata() == null)
                throw new IllegalStateException("Processing Stage class " + factory.getClass().getName() +
                        " does not have query processor metadata");

            this.processingChain.add(i, factory);
            queryProcessorMetadata.addAll(factory.getQueryProcessorMetadata());
            this.queryProcessingStageMetadata.add(new QueryProcessingStageMetadata(annotation.name(), annotation.label(), queryProcessorMetadata));
        }
    }

    @Override
    public QueryProcessingStageFactory<?> getQueryProcessingStageFactory(Class<?> queryProcessorFamily)
    {
        for (QueryProcessingStageFactory<?> factory : processingChain)
            if (factory.getQueryProcessorFamily() == queryProcessorFamily)
                return factory;
        return null;
    }

    @Override
    public QueryProcessingStageFactory<?> getQueryProcessingStageFactory(String queryProcessorFamilyName)
    {
        for (QueryProcessingStageFactory<?> factory : processingChain)
        {
            String factoryName = factory.getClass().getAnnotation(QueryProcessingStage.class).name();
            if (factoryName.equalsIgnoreCase(queryProcessorFamilyName))
                return factory;
        }
        return null;
    }

    @Override
    public ImmutableList<QueryProcessingStageMetadata> getQueryProcessingChainMetadata()
    {
        return new ImmutableList.Builder<QueryProcessingStageMetadata>().addAll(queryProcessingStageMetadata).build();
    }
}
