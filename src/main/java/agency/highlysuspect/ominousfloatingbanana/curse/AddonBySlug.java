package agency.highlysuspect.ominousfloatingbanana.curse;

import agency.highlysuspect.ominousfloatingbanana.Init;
import agency.highlysuspect.ominousfloatingbanana.curse.types.Addon;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record AddonBySlug(String slug) implements GraphqlRequest<Addon> {
	@Override
	public JsonObject requestToJson() {
		JsonObject j = new JsonObject();
		j.addProperty("query", """
			query banana($slug: String) {
				addons(slug: $slug) {
					id
					categorySection {
						id
					}
				}
			}""");
		j.addProperty("operationName", "banana");
		
		JsonObject variables = new JsonObject();
		variables.addProperty("slug", slug);
		j.add("variables", variables);
		
		return j;
	}
	
	@Override
	public Addon parse(JsonElement value) {
		JsonArray addons = value.getAsJsonObject().get("data").getAsJsonObject().get("addons").getAsJsonArray();
		
		if(addons.isEmpty()) {
			throw new RuntimeException("Zero addons returned for slug " + slug);
		}
		
		Addon addon = Init.GSON.fromJson(addons.get(0), Addon.class);
		
		if(addon.categorySection.id != 8) {
			throw new RuntimeException("Slug " + slug + " is not a Minecraft mod");
		}
		
		return addon;
	}
}
