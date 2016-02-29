package org.baderlab.autoannotate.internal.labels;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelEvents;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Remember factory and context per AnnotationSet?
 * 
 * Generating a single label (merge clusters, create cluster manually) should
 * use the last chosen settings from the dialog.
 * 
 * Regenerating labels for a list of clusters should pop-up the dialog,
 * with settings remembered.
 * 
 * @author mkucera
 *
 */
@Singleton
public class LabelMakerManager {

	@BindingAnnotation @Retention(RUNTIME) public @interface DefaultFactory {}
	
	
	@Inject private Map<String, LabelMakerFactory<?>> allFactories;
	@Inject private @DefaultFactory String defaultFactory;
	
	private Map<AnnotationSet,LabelMakerFactory<?>> factories = new HashMap<>();
	private Map<AnnotationSet, Map<LabelMakerFactory<?>, Object>> contexts = new HashMap<>();
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetDeleted event) {
		AnnotationSet as = event.getAnnotationSet();
		factories.remove(as);
		contexts.remove(as);
	}
	
	public void register(AnnotationSet as, LabelMakerFactory<?> factory, Object context) {
		factories.put(as, factory);
		Map<LabelMakerFactory<?>, Object> lmfc = contexts.get(as);
		if(lmfc == null) {
			lmfc = new HashMap<>();
			contexts.put(as, lmfc);
		}
		lmfc.put(factory, context);
	}
	
	
	public List<LabelMakerFactory<?>> getFactories() {
		return allFactories
				.values()
				.stream()
				.sorted((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()))
				.collect(Collectors.toList());
	}
	
	public LabelMakerFactory<?> getDefaultFactory() {
		return allFactories.get(defaultFactory);
	}
	
	public LabelMakerFactory<?> getFactory(AnnotationSet as) {
		LabelMakerFactory<?> factory = factories.get(as);
		return factory == null ? getDefaultFactory() : factory;
	}
	
	
	public Object getContext(AnnotationSet as, LabelMakerFactory<?> factory) {
		Map<LabelMakerFactory<?>, Object> lmfc = contexts.get(as);
		if(lmfc == null)
			return factory.getDefaultContext();
		Object context = lmfc.get(factory);
		return context == null ? factory.getDefaultContext() : context;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LabelMaker getLabelMaker(AnnotationSet as) {
		LabelMakerFactory factory = getFactory(as);
		Object context = getContext(as, factory);
		return factory.createLabelMaker(context);
	}
	
}
