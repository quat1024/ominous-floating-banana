package agency.highlysuspect.ominousfloatingbanana.graphql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Small helper class so you can build GraphQL queries in a slightly more dignified way than string interpolation.
 * Has basically no features besides the things I need in order to interact with CurseProxy, lol.
 * Does not try to help you not shoot yourself in the foot.
 * I'm not well-versed on graphql terminology so this is probably more than a little bit wrong.
 */
public class GField {
	//Real quick dsl for preparing a GraphQL query
	
	public GField(String name) {
		this.name = name;
	}
	
	public final String name;
	public final List<GField> subfields = new ArrayList<>();
	public final List<GField.Prop> arguments = new ArrayList<>();
	
	public GField nest(GField... children) {
		subfields.addAll(Arrays.asList(children));
		return this;
	}
	
	public GField prop(String key, Object value) {
		arguments.add(new Prop(key, value));
		return this;
	}
	
	//Converting the query to a string
	
	public String stringifyToQueryNamed(String queryName) {
		StringBuilder result = new StringBuilder();
		recursiveStringify(result, 1);
		return "query " + queryName + " {\n" + result + "}";
	}
	
	public String stringifyToQuery() {
		StringBuilder result = new StringBuilder();
		recursiveStringify(result, 0);
		return result.toString();
	}
	
	private void recursiveStringify(StringBuilder builder, int indentation) {
		//append the field name
		builder.append("\t".repeat(indentation));
		builder.append(name);
		
		//append any field arguments
		if(!arguments.isEmpty()) {
			builder.append('(');
			//i'm not actually sure how multiple field arguments are supposed to be put together in graphql? lol
			//assuming they're comma-separated
			builder.append(arguments.stream().map(Prop::toString).collect(Collectors.joining(", ")));
			builder.append(')');
		}
		
		//recurse and append any subfields
		if(!subfields.isEmpty()) {
			builder.append(" {\n");
			for(GField field : subfields) field.recursiveStringify(builder, indentation + 1);
			builder.append("\t".repeat(indentation));
			builder.append("}");
		}
		
		builder.append("\n");
	}
	
	//Too lazy to add a proper type-system here; for my use-case it's fine.
	//I'm assuming that Strings used as property values need to be double-quoted and escaped,
	//and anything else is probably something like "true", or a number, so it doesn't need to be escaped.
	public static record Prop(String key, Object value) {
		@Override
		public String toString() {
			return key + ": " + (value instanceof String s ? "\"" + crapEscape(s) + "\"" : value);
		}
		
		private static String crapEscape(String a) {
			return a.replace("\\", "\\\\").replace("\t", "\\t").replace("\b", "\\b").replace("\n", "\\n").replace("\r", "\\r").replace("\f", "\\f").replace("'", "\\'").replace("\"", "\\\"");
		}
	}
	
	public static void main(String[] args) {
		GField f =
			new GField("addons").prop("slug", "botania").nest(
				new GField("id"), 
				new GField("categorySection").nest(
					new GField("id")
				)
			);

		System.out.println(f.stringifyToQueryNamed("banana"));
		System.out.println(f.stringifyToQuery());
	}
}
