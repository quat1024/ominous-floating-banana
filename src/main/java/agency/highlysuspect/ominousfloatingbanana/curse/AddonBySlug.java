package agency.highlysuspect.ominousfloatingbanana.curse;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public record AddonBySlug(String slug) implements GraphqlRequest<CurseProxy.Addon> {
	@Override
	public JsonObject requestToJson() {
		JsonObject j = new JsonObject();
		j.add("query", """
			query banana($slug: String) {
				addons(slug: $slug) {
					id
					categorySection {
						id
					}
				}
			}""");
		j.add("operationName", "banana");
		
		JsonObject variables = new JsonObject();
		variables.add("slug", slug);
		j.add("variables", variables);
		
		return j;
	}
	
	@Override
	public CurseProxy.Addon parse(JsonValue value) {
		JsonArray addons = value.asObject().get("data").asObject().get("addons").asArray();
		
		if(addons.isEmpty()) {
			throw new RuntimeException("Zero addons returned for slug " + slug);
		}
		
		JsonObject addon = addons.get(0).asObject();
		int id = addon.get("id").asInt();
		int categorySectionId = addon.get("categorySection").asObject().get("id").asInt();
		
		if(categorySectionId != 8) {
			throw new RuntimeException("Slug " + slug + " is not a Minecraft mod");
		}
		
		return new CurseProxy.Addon(
			id,
			new CurseProxy.CategorySection(
				categorySectionId
			)
		);
	}
}
