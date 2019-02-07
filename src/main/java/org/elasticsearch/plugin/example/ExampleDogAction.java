package org.elasticsearch.plugin.example;

import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.cat.AbstractCatAction;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

import java.io.IOException;
import java.util.List;

/**
 * Example of adding a cat action with a plugin.
 */
public class ExampleDogAction extends BaseRestHandler {

    private static final String DOG = "-`@`-";
    private static final String DOG_NL = DOG + "\n";
    private final String HELP;

    @Inject
    ExampleDogAction(final Settings settings, final RestController controller) {
        super(settings);
        controller.registerHandler(GET, "/_dog", this);
        controller.registerHandler(GET, "/_dob/example", this);
        controller.registerHandler(POST, "/_dog/example", this);
        StringBuilder sb = new StringBuilder();
        sb.append(DOG_NL);
        sb.append("Hello form dog example action.");
        // for (AbstractCatAction catAction : catActions) {
        //     catAction.documentation(sb);
        // }
        HELP = sb.toString();
    }

    @Override
    public String getName() {
        return "rest_handler_dog_example";
    }

        @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        # TODO: Clusterの情報を引っ張ってきてNodeのの中のShardの情報を引っ張り出したい。分布状況を見えるようにしたい
        final ClusterStateRequest clusterStateRequest = new ClusterStateRequest();
        clusterStateRequest.clear().nodes(true);
        clusterStateRequest.local(request.paramAsBoolean("local", clusterStateRequest.local()));
        clusterStateRequest.masterNodeTimeout(request.paramAsTime("master_timeout", clusterStateRequest.masterNodeTimeout()));


        return channel -> channel.sendResponse(new BytesRestResponse(RestStatus.OK, HELP));
    }

    /**
    @Override
    protected RestChannelConsumer doCatRequest(final RestRequest request, final NodeClient client) {
        final String message = request.param("message", "Hello from Dogt Example action");

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
    **/
}
