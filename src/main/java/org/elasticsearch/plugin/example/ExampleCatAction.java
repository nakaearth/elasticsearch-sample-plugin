package org.elasticsearch.plugin.example;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Table;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.cat.AbstractCatAction;
import org.elasticsearch.rest.action.cat.RestTable;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 * Example of adding a cat action with a plugin.
 */
public class ExampleCatAction extends AbstractCatAction {

    ExampleCatAction(final Settings settings, final RestController controller) {
        super(settings);
        controller.registerHandler(GET, "/_cat/example", this);
        controller.registerHandler(POST, "/_cat/example", this);
    }

    @Override
    public String getName() {
        return "rest_handler_cat_example";
    }

    @Override
    protected RestChannelConsumer doCatRequest(final RestRequest request, final NodeClient client) {
        final String message = request.param("message", "Hello from Cat Example action");

        Table table = getTableWithHeader(request);
        table.startRow();
        table.addCell(message);
        table.endRow();
        return channel -> {
            try {
                channel.sendResponse(RestTable.buildResponse(table, channel));
            } catch (final Exception e) {
                channel.sendResponse(new BytesRestResponse(channel, e));
            }
        };
    }

    @Override
    protected void documentation(StringBuilder sb) {
        sb.append(documentation());
    }

    public static String documentation() {
        return "/_cat/example\n";
    }

    @Override
    protected Table getTableWithHeader(RestRequest request) {
        final Table table = new Table();
        table.startHeaders();
        table.addCell("test", "desc:test");
        table.endHeaders();
        return table;
    }
}
