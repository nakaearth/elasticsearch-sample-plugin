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
import org.elasticsearch.common.Table;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.PluginInfo;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.action.RestActionListener;
import org.elasticsearch.rest.action.RestResponseListener;
import org.elasticsearch.rest.action.cat.RestTable;

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
        // TODO: Clusterの情報を引っ張ってきてNodeのの中のShardの情報を引っ張り出したい。分布状況を見えるようにしたい
        final ClusterStateRequest clusterStateRequest = new ClusterStateRequest();
        clusterStateRequest.clear().nodes(true);
        clusterStateRequest.local(request.paramAsBoolean("local", clusterStateRequest.local()));
        clusterStateRequest.masterNodeTimeout(request.paramAsTime("master_timeout", clusterStateRequest.masterNodeTimeout()));

        return channel -> client.admin().cluster().state(clusterStateRequest, new RestActionListener<ClusterStateResponse>(channel) {
            @Override
            public void processResponse(final ClusterStateResponse clusterStateResponse) throws Exception {
                NodesInfoRequest nodesInfoRequest = new NodesInfoRequest();
                nodesInfoRequest.clear().plugins(true);
                client.admin().cluster().nodesInfo(nodesInfoRequest, new RestResponseListener<NodesInfoResponse>(channel) {
                    @Override
                    public RestResponse buildResponse(final NodesInfoResponse nodesInfoResponse) throws Exception {
                        return RestTable.buildResponse(buildTable(request, clusterStateResponse, nodesInfoResponse), channel);
                    }
                });
            }
        });
    }

    protected Table getTableWithHeader(final RestRequest request) {
        Table table = new Table();
        table.startHeaders();
        table.addCell("id", "default:false;desc:unique node id");
        table.addCell("name", "alias:n;desc:node name");
        table.addCell("component", "alias:c;desc:component");
        table.addCell("version", "alias:v;desc:component version");
        table.addCell("description", "alias:d;default:false;desc:plugin details");
        table.endHeaders();
        return table;
    }


    private Table buildTable(RestRequest req, ClusterStateResponse state, NodesInfoResponse nodesInfo) {
        DiscoveryNodes nodes = state.getState().nodes();
        Table table = getTableWithHeader(req);

        for (DiscoveryNode node : nodes) {
            NodeInfo info = nodesInfo.getNodesMap().get(node.getId());

            for (PluginInfo pluginInfo : info.getPlugins().getPluginInfos()) {
                table.startRow();
                table.addCell(node.getId());
                table.addCell(node.getName());
                table.addCell(pluginInfo.getName());
                table.addCell(pluginInfo.getVersion());
                table.addCell(pluginInfo.getDescription());
                table.endRow();
            }
        }

        return table;
    }
}
