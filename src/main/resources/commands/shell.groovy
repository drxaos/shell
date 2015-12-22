
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.plugin.CRaSHPlugin
import org.crsh.plugin.PropertyDescriptor
import org.crsh.text.Color
import org.crsh.text.ui.UIBuilder

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Usage("shell related command")
class shell {

  static def STATUS_MAP = [
      (CRaSHPlugin.CONSTRUCTED):"constructed",
      (CRaSHPlugin.FAILED):"failed",
      (CRaSHPlugin.INITIALIZED):"initialized",
      (CRaSHPlugin.INITIALIZING):"initializing"
  ];

  static def STATUS_COLOR = [
      (CRaSHPlugin.CONSTRUCTED): Color.blue,
      (CRaSHPlugin.FAILED):Color.red,
      (CRaSHPlugin.INITIALIZED):Color.green,
      (CRaSHPlugin.INITIALIZING):Color.yellow
  ];

  @Usage("list the loaded plugins and their configuration")
  @Command
  public Object plugins() {
    def table = new UIBuilder().table(rightCellPadding: 1) {
      crash.context.plugins.each() { plugin ->
        header(bold: true, fg: black, bg: white) {
          table(rightCellPadding: 1) {
            row {
              label("$plugin.type.simpleName")
              label(fg: STATUS_COLOR[plugin.status], "(${STATUS_MAP[plugin.status]})")
            }
          }
        }
        def capabilities = plugin.configurationCapabilities
        if (capabilities.iterator().hasNext()) {
          row {
            table(columns: [2,2,1,1], rightCellPadding: 1) {
              header {
                label("name"); label("description"); label("type"); label("default")
              }
              capabilities.each { desc ->
                row {
                  label(desc.name); label(desc.description); label(desc.type.simpleName); label(desc.defaultDisplayValue)
                }
              }
            }
          }
        }
      }
    }
    return table;
  }

  @Usage("list the configuration properties and their description")
  @Command
  public Object properties() {
    def capabilities = PropertyDescriptor.ALL.values()
    def table = new UIBuilder().table(rightCellPadding: 1) {
      header(bold: true, fg: black, bg: white) {
        label("name"); label("description"); label("type"); label("value"); label("default")
      }
      capabilities.each { desc ->
        def property = crash.context.propertyManager.getProperty(desc);
        String value = property != null ? property.displayValue : "";
        row {
          label(desc.name); label(desc.description); label(desc.type.simpleName); label(value); label(desc.defaultValue)
        }
      }
    }
    return table
  }
}
