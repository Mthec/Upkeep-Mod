import javassist.ClassPool;
import org.junit.Test;

// TODO - Wouldn't these only catch errors if the compiler went crazy?
public class Questions {
    @Test
    public void testLoadVillageFoundationQuestion() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        String path = "out/production/NoMinimumUpkeep/com/wurmonline/server/questions/VillageFoundationQuestion.class";
        pool.makeClass(path);
        pool.get("com.wurmonline.server.questions.VillageFoundationQuestion");
    }

    @Test
    public void testLoadVillageUpkeep() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        String path = "out/production/NoMinimumUpkeep/com/wurmonline/server/questions/VillageUpkeep.class";
        pool.makeClass(path);
        pool.get("com.wurmonline.server.questions.VillageUpkeep");
    }
}
