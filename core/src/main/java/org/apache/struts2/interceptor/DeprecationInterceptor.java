package org.apache.struts2.interceptor;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.struts2.StrutsConstants;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.XWorkConstants;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

/**
 * 
 * Deprecation Interceptor
 * <br/><br />
 * In dev mode validates whether application uses deprecated or unknown constants
 * and displays warning.
 *
 */
public class DeprecationInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;
  protected static final Logger LOG = LoggerFactory.getLogger(DeprecationInterceptor.class);
  private Container container;
  private boolean hasDeprecated;
  private boolean validated;
  private boolean devMode;

  private String message;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    validate();
    if (devMode && hasDeprecated)
      LOG.info(message);
    return invocation.invoke();
  }

  /**
   * Validates constants. Validation goes on only if devMode is set.
   * @throws Exception
   */
  private void validate() throws Exception {
    if (validated)
      return;

    if (devMode) {
      Set<String> strutsConstants = new HashSet<String>();

      for (Field field : StrutsConstants.class.getDeclaredFields())
        if (String.class.equals(field.getType()))
          strutsConstants.add((String)field.get(StrutsConstants.class));

      for (Field field : XWorkConstants.class.getDeclaredFields())
        if (String.class.equals(field.getType()))
          strutsConstants.add((String)field.get(XWorkConstants.class));

      Set<String> applicationConstants = container.getInstanceNames(String.class);
      if (!strutsConstants.containsAll(applicationConstants)) {
        hasDeprecated = true;
        Set<String> deprecated = new HashSet<String>(applicationConstants);
        deprecated.removeAll(strutsConstants);
        prepareMessage(deprecated);
      }
    }
    validated = true;
  }

  /**
   * Prepares message to display
   * @param deprecated A set with deprecated/unknown constants
   */
  private void prepareMessage(Set<String> deprecated) {
    StringBuilder sb = new StringBuilder("\n");
    sb.append("*************************************************************************************\n");
    sb.append("*************************************************************************************\n");
    sb.append("*************************************************************************************\n");
    sb.append("*****                                                                           *****\n");
    sb.append("*****                               WARNING                                     *****\n");
    sb.append("*****                YOU USE DEPRECATED / UNKNOWN CONSTANTS                     *****\n");
    sb.append("*****                                                                           *****\n");

    for (String dep : deprecated)
      sb.append(String.format("*****  -> %-69s *****\n", dep));

    sb.append("*************************************************************************************\n");
    sb.append("*************************************************************************************\n");
    sb.append("*************************************************************************************\n");

    message = sb.toString();
  }

  @Inject(StrutsConstants.STRUTS_DEVMODE)
  public void setDevMode(String state) {
    this.devMode = "true".equals(state);
  }

  @Inject
  public void setContainer(Container container) {
    this.container = container;
  }

}
