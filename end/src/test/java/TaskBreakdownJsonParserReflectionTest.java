import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskBreakdownJsonParserReflectionTest {

    @Test
    void parseShouldFallbackInvalidAssigneeId() throws Exception {
        Class<?> parserClass = Class.forName("com.example.end.agent.TaskBreakdownJsonParser");
        Object parser = parserClass.getConstructor(ObjectMapper.class).newInstance(new ObjectMapper());

        String content = """
                {
                  "summary": "测试总结",
                  "items": [
                    {
                      "title": "任务1",
                      "description": "说明",
                      "priority": "高",
                      "suggestedDays": 3,
                      "assigneeId": 99
                    }
                  ]
                }
                """;

        Object result = parserClass.getMethod("parse", String.class).invoke(parser, content);
        Object items = result.getClass().getMethod("getItems").invoke(result);
        Object item = ((java.util.List<?>) items).get(0);
        Method getAssigneeId = item.getClass().getMethod("getAssigneeId");

        assertEquals(1, getAssigneeId.invoke(item));
    }
}
