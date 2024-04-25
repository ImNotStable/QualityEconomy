package com.imnotstable.qualityeconomy;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.bukkit.configuration.file.YamlConfiguration;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class QualityLoader implements PluginLoader {
  
  @Override
  public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
    MavenLibraryResolver resolver = new MavenLibraryResolver();
    resolveLibraries(classpathBuilder).forEach(library -> resolver.addDependency(new Dependency(new DefaultArtifact(library), null)));
    resolver.addRepository(new RemoteRepository.Builder("maven", "default", "https://repo1.maven.org/maven2/").build());
    classpathBuilder.addLibrary(resolver);
  }
  
  @NotNull
  private static List<String> resolveLibraries(@NotNull PluginClasspathBuilder classpathBuilder) {
    try (InputStream input = QualityLoader.class.getClassLoader().getResourceAsStream("paper-libraries.yml")) {
      if (input == null)
        return List.of();
      try (InputStreamReader reader = new InputStreamReader(input)) {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
        return configuration.getStringList("libraries");
      }
    } catch (Throwable exception) {
      classpathBuilder.getContext().getLogger().error("Failed to resolve libraries", exception);
    }
    return List.of();
  }
  
}
