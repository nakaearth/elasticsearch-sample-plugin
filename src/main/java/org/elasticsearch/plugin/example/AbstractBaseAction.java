package org.elasticsearch.plugin.example;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Table;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.io.UTF8StreamWriter;
import org.elasticsearch.common.io.stream.BytesStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.elasticsearch.rest.action.cat.RestTable.buildHelpWidths;
import static org.elasticsearch.rest.action.cat.RestTable.pad;

public abstract class AbstractBaseAction extends BaseRestHandler {
    public AbstractBaseAction(Settings settings) {
        super(settings);
    }

    protected abstract RestChannelConsumer doRequest(RestRequest request, NodeClient client);

    protected abstract void documentation(StringBuilder sb);

    protected abstract Table getTableWithHeader(RestRequest request);

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        boolean helpWanted = request.paramAsBoolean("help", false);
        if (helpWanted) {
            return channel -> {
                Table table = getTableWithHeader(request);
                int[] width = buildHelpWidths(table, request);
                BytesStream bytesOutput = Streams.flushOnCloseStream(channel.bytesOutput());
                UTF8StreamWriter out = new UTF8StreamWriter().setOutput(bytesOutput);
                for (Table.Cell cell : table.getHeaders()) {
                    // need to do left-align always, so create new cells
                    pad(new Table.Cell(cell.value), width[0], request, out);
                    out.append(" | ");
                    pad(new Table.Cell(cell.attr.containsKey("alias") ? cell.attr.get("alias") : ""), width[1], request,
                            out);
                    out.append(" | ");
                    pad(new Table.Cell(cell.attr.containsKey("desc") ? cell.attr.get("desc") : "not available"),
                            width[2], request, out);
                    out.append("\n");
                }
                out.close();
                channel.sendResponse(
                        new BytesRestResponse(RestStatus.OK, BytesRestResponse.TEXT_CONTENT_TYPE, bytesOutput.bytes()));
            };
        } else {
            return doRequest(request, client);
        }
    }

    static Set<String> RESPONSE_PARAMS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("format", "h", "v", "ts", "pri", "bytes", "size", "time", "s")));

    @Override
    protected Set<String> responseParams() {
        return RESPONSE_PARAMS;
    }

}