import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.questions.VillageUpkeep;
import javassist.ClassPool;
import javassist.CtClass;
import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.mockito.Mockito.*;

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

    // TODO - Comment, what does this do again?
    @Test
    public void testCrazy() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass player = pool.get("com.wurmonline.server.players.Player");
        player.setModifiers(Modifier.PUBLIC);
        Class newPlayer = player.toClass();
        pool.get("com.wurmonline.server.questions.VillageUpkeep").detach();
        String path = "out/production/NoMinimumUpkeep/com/wurmonline/server/questions/VillageUpkeep.class";
        pool.makeClass(path);
        pool.get("com.wurmonline.server.questions.VillageUpkeep").toClass();
        Servers.loadAllServers(false);
        Servers.localServer.testServer = false;
        Creature creature = (Creature)mock(newPlayer);
        VillageUpkeep v = new VillageUpkeep(creature, "TITLE", "", 1);
        Communicator comm = mock(Communicator.class);
        when(creature.getCommunicator()).thenReturn(comm);
        v.sendQuestion();
        verify(comm).sendBml(500, 400, true, true, "", 200, 200, 200, "TITLE");
    }
}
