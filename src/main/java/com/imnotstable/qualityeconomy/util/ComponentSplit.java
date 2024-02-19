package com.imnotstable.qualityeconomy.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ComponentSplit {
  
  private ComponentSplit() {
  }

  @Contract(pure = true)
  public static @NotNull List<Component> split(final @NotNull Component self, final @NotNull @RegExp String separator) {
    // First split component content
    List<Component> lines = splitComponentContent(self, separator);
    
    if (self.children().isEmpty()) {
      return lines;
    }
    
    // Extract last split, which will contain all children of the same line
    Component parent = lines.remove(lines.size() - 1);
    
    // Process each child in order
    for (Component child : self.children()) {
      // Split child to List<Component>
      List<Component> childSegments = split(child, separator);
      
      // each split will be a new row, except the first which will stick to the parent
      parent = parent.append(childSegments.get(0));
      for (int i = 1; i < childSegments.size(); i++) {
        lines.add(parent);
        parent = Component.empty().style(parent.style());
        parent = parent.append(childSegments.get(i));
      }
    }
    lines.add(parent);
    return lines;
  }
  
  private static List<Component> splitComponentContent(Component component, @RegExp String regex) {
    if (!(component instanceof TextComponent t)) {
      return List.of(component);
    }
    String[] segments = t.content().split(regex);
    if (segments.length == 0) {
      // Special case if the split regex is equals to the content.
      segments = new String[]{"", ""};
    }
    return Arrays.stream(segments)
      .map(s -> Component.text(s).style(t.style()))
      .map(c -> (Component) c)
      .collect(Collectors.toList());
  }
  
}
