package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FireGenUserSerializer extends JsonSerializer<FireGenUser<?>> {

    @Override
    public void serialize(FireGenUser<?> user, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeNumberField("id", user.getId());
        gen.writeStringField("name", user.getName());
        gen.writeStringField("login", user.getLoginUsername());

        gen.writeArrayFieldStart("permissions");
        List<Permission> permissions = user.getPermissions();
        if (permissions.size() == 1 && permissions.getFirst().equals(Permission.ADMIN)) {
            permissions.clear();
            permissions.addAll(Arrays.stream(Permission.values()).toList());
        }
        for (Permission permission : permissions) {
            gen.writeString(permission.name());
        }
        gen.writeEndArray();

        gen.writeEndObject();
    }

}
