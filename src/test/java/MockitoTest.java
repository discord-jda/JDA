import net.dv8tion.jda.api.EmbedBuilder;
// import net.dv8tion.jda.api.audit.ThreadLocalReason;
// import net.dv8tion.jda.api.events.ExceptionEvent;
// import net.dv8tion.jda.api.events.http.HttpRequestEvent;
// import net.dv8tion.jda.api.exceptions.ContextException;
// import net.dv8tion.jda.api.exceptions.ErrorResponseException;
// import net.dv8tion.jda.api.exceptions.RateLimitedException;
// import net.dv8tion.jda.api.requests.Request;
// import net.dv8tion.jda.internal.JDAImpl;
// import net.dv8tion.jda.internal.requests.CallbackContext;
// import net.dv8tion.jda.internal.requests.RestActionImpl;
// import net.dv8tion.jda.internal.requests.Route;
// import net.dv8tion.jda.internal.utils.IOUtil;
// import okhttp3.MultipartBody;
// import okhttp3.RequestBody;
// import org.apache.commons.collections4.map.CaseInsensitiveMap;

// import javax.annotation.Nonnull;
// import javax.annotation.Nullable;
// import javax.print.attribute.standard.ColorSupported;
// import javax.xml.ws.Response;

// import java.util.concurrent.CancellationException;
// import java.util.concurrent.TimeoutException;
// import java.util.function.BooleanSupplier;
// import java.util.function.Consumer;


import org.mockito.Mock;
//import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
// import static org.mockito.MockitoAnnotations.initMocks;


// import net.dv8tion.jda.api.entities.EmbedType;
// import net.dv8tion.jda.api.entities.MessageEmbed;
// import net.dv8tion.jda.api.entities.Role;
// import net.dv8tion.jda.internal.entities.EntityBuilder;
// import net.dv8tion.jda.internal.utils.Checks;
// import net.dv8tion.jda.internal.utils.Helpers;

// import javax.annotation.Nonnull;
// import javax.annotation.Nullable;
import java.awt.*;
// import java.time.OffsetDateTime;
// import java.time.temporal.TemporalAccessor;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.regex.Pattern;



/*
 * Class Mockito Test
 * Simple test suit for selected mocking 
 */

public class MockitoTest {
    @Mock
    Color color;
    int col;
    EmbedBuilder mockEmbed;

    @BeforeEach
    public void setup() {
        color = mock(Color.class);
        col = mock(Integer.class);
        mockEmbed = mock(EmbedBuilder.class);
    }

    @Test
    public void mockitoTestColor() throws Exception {
        assertNotNull(color);
        mockEmbed.setColor(color);
        verify(mockEmbed).setColor(color);
    }

    @Test
    public void mockitoTestColor1() throws Exception {
        assertNotNull(color);
        EmbedBuilder realEmbed = new EmbedBuilder();
        assertEquals(realEmbed.setColor(color), mockEmbed.setColor(color));
    }


    @Test
    public void mockitoTestColorInt() throws Exception {
        assertNotNull(col);
        mockEmbed.setColor(col);
        verify(mockEmbed).setColor(col);
    }

    @Test
    public void mockitoTestColorInt1() throws Exception {
        assertNotNull(col);
        EmbedBuilder realEmbed = new EmbedBuilder();
        assertEquals(realEmbed.setColor(col), mockEmbed.setColor(col));
    }

}
