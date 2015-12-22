import org.crsh.cli.Command
import org.crsh.cli.Option
import org.crsh.cli.Usage
import org.crsh.command.Pipe

class sort {

  @Usage("sort a map")
  @Command
  Pipe<Map, Map> main(
      @Usage("Filed used to sort")
      @Option(names = ['f', 'fields']) List<String> fields) {
    return new Pipe<Map, Map>() {
      List<Map> d = new ArrayList<Map>();

      @Override
      void provide(Map element) {
        d.add(element);
      }

      @Override
      void flush() {
        Collections.sort(d, new EntryComparator(fields))
        d.each { m ->
          context.provide(m);
        }
        d.clear();
        super.flush();
      }
    }
  }

  class EntryComparator implements Comparator<Map> {

    List<String> fields;

    EntryComparator(List<String> fields) {
      this.fields = fields
    }

    int compare(Map o1, Map o2) {

      for (String field : fields) {

        int order = 1;
        if (field.endsWith(":asc")) {
          field = field.substring(0, field.length() - 4)
        }
        if (field.endsWith(":desc")) {
          field = field.substring(0, field.length() - 5)
          order = -1;
        }

        if (o1.containsKey(field) && o2.containsKey(field)) {
          def v1 = o1.get(field);
          def v2 = o2.get(field);
          if (v1 instanceof Comparable && v2 instanceof Comparable) {
            int r = v1.compareTo(v2);
            if (r != 0) {
              return r * order;
            }
          }
        }
        else {
          return 0;
        }
      }

      return 0;
    }

  }

}