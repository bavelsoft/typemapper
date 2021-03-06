package com.bavelsoft.typemapper.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Collections;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.annotation.processing.Filer;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.Diagnostic;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import com.bavelsoft.typemapper.ExpectedException;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.exception.ExceptionUtils;

import static java.util.Arrays.asList;

/*
 * Entry point, handles annotation processing api, calls Generator for the real work
 */
@AutoService(javax.annotation.processing.Processor.class)
public class Processor extends AbstractProcessor {
	private Messager messager;
	private Elements elementUtils;
	private Types typeUtils;
	private Filer filer;
	private Generator generator;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);
		messager = env.getMessager();
		elementUtils = env.getElementUtils();
		typeUtils = env.getTypeUtils();
		filer = env.getFiler();
		generator = new Generator(elementUtils, typeUtils);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotationsParam, RoundEnvironment env) {
		Set<Element> elements = new HashSet<>();
		for (Element element : env.getElementsAnnotatedWith(FieldMatcher.typeMapClass))
			elements.add(element.getEnclosingElement());
		for (Element element : elements) {
			try {
				write(element, generator.generateMapperClass(element).build());
			} catch (ExpectedException e) {
				messager.printMessage(Diagnostic.Kind.ERROR,
						      "couldn't generate field mapper for "+element+" : "+ e.getMessage());
			} catch (Exception e) {
				messager.printMessage(Diagnostic.Kind.ERROR,
						      "couldn't generate field mapper for "+element+" : "+ ExceptionUtils.getStackTrace(e));
                	} 
		}
		return true;
	}

	private void write(Element element, TypeSpec typeSpec) throws IOException {
		String packageName = elementUtils.getPackageOf(element).toString();
		JavaFileObject javaFileObject = filer.createSourceFile(packageName+"."+generator.getClassName(element));
		Writer writer = javaFileObject.openWriter();
		JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
		javaFile.writeTo(writer);
		writer.close();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(FieldMatcher.typeMapClass.getCanonicalName().toString());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
}
