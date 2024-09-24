/*-
 * #%L
 * Lite Renderer Add-On
 * %%
 * Copyright (C) 2024 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.flowingcode.vaadin.addons.litetemplate;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

class PropertyEncoder {

  private enum Context {

    INITIAL,
    ESCAPED,
    QUOT,
    APOS,
    TEMPLATE_HEAD,
    EXPRESSION,
    NESTED_TEMPLATE;

    private static Context pop(Deque<Context> stack) {
      stack.pop();
      return null;
    }

    static Optional<String> encode(String s, String prefix, String suffix) {
      StringBuilder sb = new StringBuilder();

      int templateBegin = -1;
      int templateEnd = -1;

      Deque<Context> stack = new ArrayDeque<>();
      stack.push(INITIAL);
      for (int i = 0, n = s.length(); i < n; i++) {
        char c = s.charAt(i);
        sb.append(c);

        switch (stack.peek()) {
          case ESCAPED:
            stack.pop();
            continue;
          case QUOT:
            if (c == '\"') {
              stack.pop();
            }
            continue;
          case APOS:
            if (c == '\'') {
              stack.pop();
            }
            continue;
          case TEMPLATE_HEAD:
            assert c == '{';
            stack.pop();
            stack.push(EXPRESSION);
            continue;
          case INITIAL:
            switch (c) {
              case '`':
                sb.setLength(sb.length() - 1);
                sb.append('\\').append(c);
                continue;
              case '$': {
                templateBegin = i;
                break;
              }
            }
            //fallthrough
          case NESTED_TEMPLATE: {
            Optional.ofNullable(switch (c) {
              case '`' -> pop(stack);
              case '$' -> (i < n - 1 && s.charAt(i + 1) == '{') ? TEMPLATE_HEAD : null;
              case '\\' -> ESCAPED;
              default -> null;
            }).ifPresent(stack::push);
            continue;
          }
          case EXPRESSION: {
            Optional.ofNullable(switch(c) {
              case '"' -> QUOT;
              case '\'' -> APOS;
              case '`' -> NESTED_TEMPLATE;
              case '{' -> EXPRESSION;
              case '}' -> {
                templateEnd = i + 1;
                yield pop(stack);
              }
              default -> null;
            }).ifPresent(stack::push);
            continue;
          }
          default:
            throw new IllegalStateException();
        }
      }

      if (stack.peek() == INITIAL) {
        String encoded = sb.toString();
        if (templateBegin != 0 || templateEnd != s.length()) {
          encoded = prefix + encoded + suffix;
        }
        return Optional.of(encoded);
      } else {
        return Optional.empty();
      }
    }
  }

  public static Optional<String> encode(String value, String prefix, String suffix) {
    return Context.encode(value, prefix, suffix);
  }

}
