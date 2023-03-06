package models;

import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import server.BasicServer;
import server.ContentType;
import server.ResponseCodes;
import utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LabRab extends BasicServer {
    private static Integer allVotes = 0;
    private static Candidate candidate = new Candidate();
    private static CandidateModel candidates = new CandidateModel();
    private final static Configuration freemarker = initFreeMarker();
    public LabRab(String host, int port) throws IOException {
        super(host, port);
        registerGet("/", this::homeHandler);
        registerPost("/vote", this::vote);
        registerGet("/thankyou", this::thankyou);
        registerGet("/votes", this::votes);
    }

    private void votes(HttpExchange exchange) {
        Map<String, Object> data = new HashMap<>();
        data.put("candidates", candidates.getCandidates().stream()
                .sorted((Comparator.comparing(Candidate::getVote)).reversed())
                .collect(Collectors.toList()));
        data.put("votes", allVotes);
        renderTemplate(exchange, "votes.html", data);
    }

    private void thankyou(HttpExchange exchange) {
        Map<String, Object> data = new HashMap<>();
        data.put("candidate", candidate);
        data.put("percent", (candidate.getVote() * 100 / allVotes));
        renderTemplate(exchange, "thankyou.html", data);
    }

    private void vote(HttpExchange exchange) {
        String raw = getBody(exchange);
        var cands = new CandidateModel().getCandidates();
        Map<String, String> parsed = Utils.parseUrlEncoded(raw, "&");
        for (int i = 0; i < cands.size(); i++) {
            if(parsed.get("candidateId").equalsIgnoreCase(candidates.getCandidates().get(i).getId())){
                cands.get(i).setVote(cands.get(i).getVote() + 1);
                candidate = cands.get(i);
                break;
            }
        }
        CandidateModel.writeFile(cands);
        allVotes++;
        redirect303(exchange, "/thankyou");
    }

    private void homeHandler(HttpExchange exchange) {
        renderTemplate(exchange, "candidates.html", candidates);
    }

    private static Configuration initFreeMarker() {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
            cfg.setDirectoryForTemplateLoading(new File("data"));
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);
            return cfg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    protected void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel) {
        try {
            Template temp = freemarker.getTemplate(templateFile);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {

                temp.process(dataModel, writer);
                writer.flush();

                var data = stream.toByteArray();

                sendByteData(exchange, ResponseCodes.OK, ContentType.TEXT_HTML, data);
            }
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }
}
