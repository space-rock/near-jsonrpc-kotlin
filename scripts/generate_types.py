"""
Generates Kotlin types and RPC methods from OpenAPI specification.
"""
import json
import os
import hashlib
from typing import Any, Dict, List, Optional, Set, Tuple

OPENAPI_PATH = "./openapi.json"
OUTPUT_TYPES_PATH = "../types/src/main/kotlin/org/near/jsonrpc/types/Types.kt"
OUTPUT_METHODS_PATH = "../client/src/main/kotlin/org/near/jsonrpc/client/Methods.kt"

KOTLIN_RESERVED_KEYWORDS = {
    "as", "break", "class", "continue", "do", "else", "false", "for", "fun",
    "if", "in", "interface", "is", "null", "object", "package", "return", "super",
    "this", "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while",
    "by", "catch", "constructor", "delegate", "dynamic", "field", "file", "finally",
    "get", "import", "init", "param", "property", "receiver", "set", "setparam", "where",
    "actual", "abstract", "annotation", "companion", "const", "crossinline", "data",
    "enum", "expect", "external", "final", "infix", "inline", "inner", "internal",
    "lateinit", "noinline", "open", "operator", "out", "override", "private", "protected",
    "public", "reified", "sealed", "suspend", "tailrec", "vararg"
}

HEADER_CODE = """@file:OptIn(ExperimentalSerializationApi::class)

package org.near.jsonrpc.types

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*

@Serializer(forClass = JsonElement::class)
object PolymorphicSerializer : KSerializer<JsonElement> {
    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor
    override fun serialize(encoder: Encoder, value: JsonElement) = JsonElement.serializer().serialize(encoder, value)
    override fun deserialize(decoder: Decoder): JsonElement = JsonElement.serializer().deserialize(decoder)
}

"""

def load_openapi() -> Dict[str, Any]:
    if not os.path.exists(OPENAPI_PATH):
        raise FileNotFoundError(f"{OPENAPI_PATH} not found")
    with open(OPENAPI_PATH, "r", encoding="utf-8") as f:
        return json.load(f)

def escape_kotlin_keyword(property_name: str) -> str:
    """Escape Kotlin reserved keywords by wrapping in backticks"""
    if property_name in KOTLIN_RESERVED_KEYWORDS:
        return f"`{property_name}`"
    return property_name

def to_kotlin_type_name(name: str) -> str:
    """Convert schema name to Kotlin type name (PascalCase)"""
    if not name:
        return name
    
    if "_" in name:
        parts = name.split("_")
        processed_parts = []
        for part in parts:
            if part.islower():
                processed_parts.append(part.capitalize())
            else:
                processed_parts.append(part)
        return "".join(processed_parts)
    
    if name and name[0].islower():
        return name[0].upper() + name[1:]
    
    return name

def to_kotlin_property_name(name: str) -> str:
    """Convert property name to Kotlin property name (camelCase)"""
    if "_" in name:
        parts = name.split("_")
        
        def transform_part(part: str) -> str:
            if not part:
                return ""
            if part.islower():
                if any(ch.isdigit() for ch in part):
                    digit_indices = [idx for idx, ch in enumerate(part) if ch.isdigit()]
                    if digit_indices:
                        has_letter_before = any(ch.isalpha() for ch in part[:digit_indices[0]])
                        has_letter_after = any(ch.isalpha() for ch in part[digit_indices[-1] + 1:])
                        if has_letter_before and has_letter_after:
                            return "".join(ch.upper() if ch.isalpha() else ch for ch in part)
                return part.capitalize()
            if part.isupper():
                return part
            return part[0].upper() + part[1:]
        
        first_part = parts[0].lower() if parts else ""
        transformed = [transform_part(part) for part in parts[1:]]
        return first_part + "".join(transformed)
    
    if name.isupper() and len(name) > 1:
        return name.lower()
    
    return name[0].lower() + name[1:] if name else name

def to_screaming_snake_case(value: str) -> str:
    """Convert a string to SCREAMING_SNAKE_CASE"""
    import re
    result = value.replace("-", "_").replace(" ", "_").replace(".", "_").replace("/", "_").replace("@", "_")
    result = re.sub(r'([a-z0-9])([A-Z])', r'\1_\2', result)
    result = re.sub(r'([A-Z]+)([A-Z][a-z])', r'\1_\2', result)
    return result.upper()

def resolve_ref_name(ref: str) -> Optional[str]:
    """Extract type name from $ref"""
    if not ref.startswith("#/components/schemas/"):
        return None
    return ref.split("/")[-1]

def resolve_ref_schema(ref: str, components: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Resolve a $ref to its schema definition"""
    name = resolve_ref_name(ref)
    if name:
        return components.get(name)
    return None

def ensure_unique_type_name(name: str, generated_types: Set[str]) -> str:
    """Ensure a type name is unique by adding suffix if needed"""
    kotlin_name = to_kotlin_type_name(name)
    original_kotlin_name = kotlin_name
    
    if kotlin_name in generated_types:
        counter = 2
        while kotlin_name in generated_types:
            kotlin_name = f"{original_kotlin_name}{counter}"
            counter += 1
    
    return kotlin_name

def register_generated_type(kotlin_name: str, generated_types: Set[str]) -> bool:
    """Register a type as generated"""
    if kotlin_name in generated_types:
        return False
    generated_types.add(kotlin_name)
    return True

def get_kotlin_primitive_type(schema: Dict[str, Any], components: Dict[str, Any], seen_refs: Optional[Set[str]] = None) -> str:
    """Map OpenAPI primitive types to Kotlin types"""
    seen_refs = seen_refs or set()
    
    typ = schema.get("type")
    fmt = schema.get("format", "")
    
    if "$ref" in schema:
        ref = schema["$ref"]
        if ref in seen_refs:
            ref_name = resolve_ref_name(ref)
            if ref_name:
                return to_kotlin_type_name(ref_name)
            return "JsonElement"
        seen_refs.add(ref)
        ref_name = resolve_ref_name(ref)
        if ref_name:
            return to_kotlin_type_name(ref_name)
        return "JsonElement"
    
    if typ == "string":
        if fmt in ("byte", "binary"):
            return "String"
        elif fmt == "uint64":
            return "Long"
        return "String"
    elif typ == "integer":
        if fmt == "int32":
            return "Int"
        elif fmt == "int64":
            return "Long"
        elif fmt == "uint64":
            return "Long"
        return "Long"
    elif typ == "number":
        if fmt == "float":
            return "Float"
        else:
            return "Double"
    elif typ == "boolean":
        return "Boolean"
    elif typ == "array":
        items = schema.get("items", {})
        items_type = get_kotlin_type(items, components, seen_refs)
        return f"List<{items_type}>"
    elif typ == "object":
        if "patternProperties" in schema and "properties" not in schema:
            pattern_props = schema.get("patternProperties", {})
            if pattern_props:
                first_pattern_schema = next(iter(pattern_props.values()))
                value_type = get_kotlin_type(first_pattern_schema, components, seen_refs)
                return f"Map<String, {value_type}>"
        if "additionalProperties" in schema and "properties" not in schema:
            if isinstance(schema["additionalProperties"], dict):
                value_type = get_kotlin_type(schema["additionalProperties"], components, seen_refs)
                return f"Map<String, {value_type}>"
            else:
                return "JsonElement"
        return "JsonElement"
    
    return "JsonElement"

def get_kotlin_type(schema: Dict[str, Any], components: Dict[str, Any], seen_refs: Optional[Set[str]] = None) -> str:
    """Get Kotlin type for a schema"""
    seen_refs = seen_refs or set()
    
    if "$ref" in schema:
        ref = schema["$ref"]
        if ref in seen_refs:
            ref_name = resolve_ref_name(ref)
            if ref_name:
                return to_kotlin_type_name(ref_name)
            return "JsonElement"
        seen_refs.add(ref)
        ref_name = resolve_ref_name(ref)
        if ref_name:
            return to_kotlin_type_name(ref_name)
        return "JsonElement"
    
    if "enum" in schema:
        return get_kotlin_primitive_type(schema, components, seen_refs)
    
    if "allOf" in schema:
        # Try to resolve the primary type from allOf
        # Usually the first ref is the main type, others are constraints/metadata
        for item in schema["allOf"]:
            if "$ref" in item:
                ref_name = resolve_ref_name(item["$ref"])
                if ref_name:
                    return to_kotlin_type_name(ref_name)
        
        # If no refs found, merge and get type
        merged = merge_allof(schema["allOf"], components)
        return get_kotlin_primitive_type(merged, components, seen_refs)
    
    if "oneOf" in schema or "anyOf" in schema:
        choices = schema.get("oneOf") or schema.get("anyOf")
        if choices:
            # Check for nullable pattern: oneOf/anyOf with null
            non_null_choices = [c for c in choices if c.get("type") != "null" and not (c.get("enum") == [None])]
            has_null = len(non_null_choices) < len(choices)
            
            if len(non_null_choices) == 1:
                # Simple nullable type
                base_type = get_kotlin_type(non_null_choices[0], components, seen_refs)
                return base_type + "?" if has_null else base_type
            elif len(non_null_choices) == 0:
                # Only null - return nullable JsonElement
                return "JsonElement?"
        return "JsonElement"
    
    return get_kotlin_primitive_type(schema, components, seen_refs)

def generate_kotlin_enum(name: str, schema: Dict[str, Any]) -> str:
    """Generate Kotlin enum for schemas with enum values"""
    kotlin_name = to_kotlin_type_name(name)
    enum_values = schema.get("enum", [])
    typ = schema.get("type", "string")
    is_nullable = schema.get("nullable", False)
    
    if not enum_values:
        return ""
    
    if len(enum_values) == 1 and enum_values[0] is None and is_nullable:
        if kotlin_name.endswith('Request'):
            return f"""typealias {kotlin_name} = Unit

"""
        else:
            return f"""@Serializable(with = {kotlin_name}Serializer::class)
object {kotlin_name}

object {kotlin_name}Serializer : KSerializer<{kotlin_name}> {{
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("{kotlin_name}")
    
    override fun serialize(encoder: Encoder, value: {kotlin_name}) {{
        encoder.encodeNull()
    }}
    
    override fun deserialize(decoder: Decoder): {kotlin_name} {{
        decoder.decodeNull()
        return {kotlin_name}
    }}
}}

"""
    
    non_null_values = [v for v in enum_values if v is not None]
    
    if not non_null_values:
        return f"""@Serializable
object {kotlin_name}

"""
    
    if typ == "string":
        code = f"@Serializable\nenum class {kotlin_name}(val value: String) {{\n"
        seen_cases = set()
        
        for value in non_null_values:
            if value is None:
                continue
            case_name = to_screaming_snake_case(str(value))
            
            if case_name and case_name[0].isdigit():
                case_name = "VAL_" + case_name
            
            original_case = case_name
            counter = 1
            while case_name in seen_cases:
                case_name = f"{original_case}_{counter}"
                counter += 1
            seen_cases.add(case_name)
            
            code += f'    @SerialName("{value}")\n'
            code += f'    {case_name}("{value}"),\n'
        
        code = code.rstrip(",\n") + ";\n"
        code += "}\n"
        return code
    else:
        code = f"@Serializable\nenum class {kotlin_name}(val value: Int) {{\n"
        seen_cases = set()
        
        for value in non_null_values:
            if value is None:
                continue
            case_name = f"VAL_{value}"
            original_case = case_name
            counter = 1
            while case_name in seen_cases:
                case_name = f"{original_case}_{counter}"
                counter += 1
            seen_cases.add(case_name)
            
            code += f'    @SerialName("{value}")\n'
            code += f'    {case_name}({value}),\n'
        
        code = code.rstrip(",\n") + ";\n"
        code += "}\n"
        return code

def merge_allof(allof_list: List[Dict[str, Any]], components: Dict[str, Any]) -> Dict[str, Any]:
    """Merge allOf schemas"""
    merged = {"properties": {}, "required": []}
    
    for item in allof_list:
        if "$ref" in item:
            resolved = resolve_ref_schema(item["$ref"], components)
            if resolved:
                # If resolved schema is a primitive type, use it directly
                if "type" in resolved and resolved["type"] in ["string", "integer", "number", "boolean"]:
                    if "properties" not in resolved:
                        return resolved.copy()
                
                if "properties" in resolved:
                    merged["properties"].update(resolved["properties"])
                if "required" in resolved:
                    merged["required"].extend(resolved["required"])
                if "type" in resolved:
                    merged["type"] = resolved["type"]
                for key in ["format", "description", "minimum", "maximum", "nullable"]:
                    if key in resolved and key not in merged:
                        merged[key] = resolved[key]
        
        if "properties" in item:
            merged["properties"].update(item["properties"])
        if "required" in item:
            merged["required"].extend(item["required"])
        if "type" in item:
            merged["type"] = item["type"]
        for key in ["format", "description", "minimum", "maximum", "nullable"]:
            if key in item and key not in merged:
                merged[key] = item[key]
    
    if "type" not in merged:
        merged["type"] = "object"
    
    merged["required"] = list(dict.fromkeys(merged["required"]))
    return merged

def generate_kotlin_data_class(name: str, schema: Dict[str, Any], components: Dict[str, Any], generated_types: Set[str]) -> str:
    """Generate Kotlin data class for object schemas"""
    kotlin_name = ensure_unique_type_name(name, generated_types)
    
    if not register_generated_type(kotlin_name, generated_types):
        return ""
    
    if "allOf" in schema:
        schema = merge_allof(schema["allOf"], components)
    
    properties = schema.get("properties", {})
    required = set(schema.get("required", []))
    
    if not properties and "additionalProperties" in schema:
        if isinstance(schema["additionalProperties"], dict):
            value_type = get_kotlin_type(schema["additionalProperties"], components)
        else:
            value_type = "JsonElement"
        return f"typealias {kotlin_name} = Map<String, {value_type}>\n\n"
    
    if not properties:
        return f"@Serializable\nobject {kotlin_name}\n\n"
    
    code = f"@Serializable\ndata class {kotlin_name}(\n"
    
    property_lines = []
    for prop_name, prop_schema in properties.items():
        kotlin_prop_name = to_kotlin_property_name(prop_name)
        kotlin_prop_name = escape_kotlin_keyword(kotlin_prop_name)
        
        prop_type = get_kotlin_type(prop_schema, components)
        
        is_nullable = prop_schema.get("nullable", False)
        is_required = prop_name in required
        
        if "$ref" in prop_schema and not is_nullable:
            ref_schema = resolve_ref_schema(prop_schema["$ref"], components.get("schemas", {}))
            if ref_schema and ref_schema.get("nullable", False):
                is_nullable = True
        
        if not is_required or is_nullable:
            if not prop_type.endswith("?"):
                prop_type += "?"
        
        clean_kotlin_prop = kotlin_prop_name.strip("`")
        
        prop_line = f'    @SerialName("{prop_name}")\n    '
        prop_line += f"val {clean_kotlin_prop}: {prop_type}"
        
        if not is_required or is_nullable:
            prop_line += " = null"
        
        property_lines.append(prop_line)
    
    code += ",\n".join(property_lines)
    
    if properties:
        code += "\n"
    
    code += ")\n\n"
    return code

def generate_inline_data_class(base_name: str, obj_schema: Dict[str, Any], components: Dict[str, Any]) -> Tuple[str, str, str]:
    """Generate an inline data class for an object schema.
    Returns: (class_name, class_code, nested_classes_code)
    """
    props = obj_schema.get("properties", {})
    required = set(obj_schema.get("required", []))
    
    prop_lines = []
    nested_classes = []
    
    for prop_name, prop_schema in props.items():
        kotlin_prop = to_kotlin_property_name(prop_name)
        kotlin_prop = escape_kotlin_keyword(kotlin_prop)
        
        # Check if this property is an inline object with properties
        if prop_schema.get("type") == "object" and "properties" in prop_schema and "$ref" not in prop_schema:
            # Generate a nested data class for this property
            nested_class_name = f"{base_name}{to_kotlin_type_name(prop_name)}"
            _, nested_props, nested_nested = generate_inline_data_class(nested_class_name, prop_schema, components)
            
            nested_classes.append(f"    @Serializable\n    data class {nested_class_name}({nested_props})\n")
            if nested_nested:
                nested_classes.append(nested_nested)
            
            prop_type = nested_class_name
        else:
            prop_type = get_kotlin_type(prop_schema, components)
        
        is_required = prop_name in required
        is_nullable = prop_schema.get("nullable", False)
        
        if not is_required or is_nullable:
            if not prop_type.endswith("?"):
                prop_type += "?"
        
        clean_kotlin_prop = kotlin_prop.strip("`")
        prop_line = f'    @SerialName("{prop_name}")\n    '
        prop_line += f"val {clean_kotlin_prop}: {prop_type}"
        
        if not is_required or is_nullable:
            prop_line += " = null"
        
        prop_lines.append(prop_line)
    
    class_code = ",\n".join(prop_lines)
    if prop_lines:
        class_code = "\n" + class_code + "\n"
    
    nested_classes_code = "\n".join(nested_classes) if nested_classes else ""
    
    return base_name, class_code, nested_classes_code

def extract_discriminator_name(variant: Dict[str, Any]) -> Optional[str]:
    """Extract discriminator value from variant for better naming"""
    if "properties" not in variant:
        return None
    
    props = variant["properties"]
    discriminator_fields = ["name", "type", "kind", "variant", "tag"]
    
    for field in discriminator_fields:
        if field in props:
            field_schema = props[field]
            if "enum" in field_schema and field_schema["enum"]:
                value = str(field_schema["enum"][0])
                parts = value.replace("-", "_").split("_")
                return "".join(p.capitalize() for p in parts if p)
    
    return None

def generate_kotlin_sealed_interface(name: str, schema: Dict[str, Any], components: Dict[str, Any], generated_types: Set[str]) -> str:
    """Generate Kotlin sealed interface for oneOf/anyOf schemas"""
    kotlin_name = to_kotlin_type_name(name)
    
    union_key = "oneOf" if "oneOf" in schema else "anyOf"
    variants = schema.get(union_key, [])
    
    if not variants:
        if not register_generated_type(kotlin_name, generated_types):
            return ""
        return f"typealias {kotlin_name} = JsonElement\n\n"
    
    if len(variants) == 1:
        variant = variants[0]
        
        if "allOf" in variant:
            # Merge allOf schemas including variant-level properties
            allof_schemas = variant["allOf"].copy()
            if "properties" in variant or "required" in variant or "type" in variant:
                inline_schema = {}
                if "properties" in variant:
                    inline_schema["properties"] = variant["properties"]
                if "required" in variant:
                    inline_schema["required"] = variant["required"]
                if "type" in variant:
                    inline_schema["type"] = variant["type"]
                allof_schemas.append(inline_schema)
            
            merged = merge_allof(allof_schemas, components)
            return generate_kotlin_data_class(name, merged, components, generated_types)
        elif "type" in variant and variant.get("type") == "object" and "properties" in variant:
            return generate_kotlin_data_class(name, variant, components, generated_types)
        elif "$ref" in variant:
            ref_name = resolve_ref_name(variant["$ref"])
            if ref_name:
                ref_kotlin_name = to_kotlin_type_name(ref_name)
                return f"typealias {kotlin_name} = {ref_kotlin_name}\n\n"
    
    if not register_generated_type(kotlin_name, generated_types):
        return ""
    
    is_reference_only_union = all("$ref" in v and len([k for k in v.keys() if k != "description"]) == 1 for v in variants)
    
    discriminator_field = None
    if not is_reference_only_union:
        # Check if all variants are objects with properties
        all_have_properties = all(v.get("type") == "object" and "properties" in v for v in variants)
        if all_have_properties:
            # Find common property name across all variants
            first_props = variants[0].get("properties", {})
            for prop_name in first_props:
                # Check if this property exists in ALL variants and has enum values
                if all(prop_name in v.get("properties", {}) for v in variants):
                    # Check if all have enum values for this property
                    all_have_enum = all(
                        "enum" in v.get("properties", {}).get(prop_name, {})
                        for v in variants
                    )
                    if all_have_enum:
                        discriminator_field = prop_name
                        break
    
    description = schema.get("description", "").strip()
    
    # Track used variant class names to ensure uniqueness
    used_variant_names = set()
    # Track mapping of wrapper keys to actual variant class names and whether they're objects/primitives
    # Format: [(wrapper_key, variant_class, is_object, is_primitive), ...]
    # is_object: True if object singleton, False if data class
    # is_primitive: True if data class wraps a single primitive/string value
    variant_serializer_map = []
    
    # Pre-calculate if we'll need a custom serializer (needed for @Serializable annotation)
    # We'll refine this later after analyzing variants
    # Don't generate serializers for allOf variants - they're just regular data classes
    all_allof_variants = all("allOf" in v for v in variants)
    will_generate_custom_serializer_preliminary = not discriminator_field and len(variants) > 1 and not all_allof_variants
    
    code = ""
    
    # Add documentation
    if description:
        first_line = description.split("\n")[0]
        if len(first_line) > 80:
            first_line = first_line[:77] + "..."
        code += f"/**\n * {first_line}\n */\n"
    
    # Generate sealed interface
    # For internally-discriminated unions, use @JsonClassDiscriminator
    # For unions with custom serializers, reference the serializer in @Serializable
    if will_generate_custom_serializer_preliminary:
        code += f"@Serializable(with = {kotlin_name}Serializer::class)\n"
    else:
        code += "@Serializable\n"
    if discriminator_field:
        code += f'@JsonClassDiscriminator("{discriminator_field}")\n'
    code += f"sealed interface {kotlin_name} {{\n"
    
    # Generate variant data classes as nested types
    for idx, variant in enumerate(variants):
        # Handle allOf variants (common in complex schemas like RpcQueryRequest)
        if "allOf" in variant:
            # Merge allOf schemas to get full structure
            # Also include any additional properties/required from the variant itself
            allof_schemas = variant["allOf"].copy()
            # If variant has its own properties, add them as an inline schema to merge
            if "properties" in variant or "required" in variant or "type" in variant:
                inline_schema = {}
                if "properties" in variant:
                    inline_schema["properties"] = variant["properties"]
                if "required" in variant:
                    inline_schema["required"] = variant["required"]
                if "type" in variant:
                    inline_schema["type"] = variant["type"]
                allof_schemas.append(inline_schema)
            
            merged = merge_allof(allof_schemas, components)
            
            # Use title as variant name if available
            variant_class_name = variant.get("title", f"Variant{idx}")
            variant_class_name = to_kotlin_type_name(variant_class_name)
            
            # Ensure unique variant name
            original_variant_name = variant_class_name
            counter = 1
            while variant_class_name in used_variant_names:
                variant_class_name = f"{original_variant_name}{counter}"
                counter += 1
            used_variant_names.add(variant_class_name)
            
            # Generate data class with merged properties
            _, inline_props, nested_classes = generate_inline_data_class(variant_class_name, merged, components)
            code += f"    @Serializable\n"
            code += f"    data class {variant_class_name}({inline_props}) : {kotlin_name}\n\n"
            if nested_classes:
                code += nested_classes + "\n"
        
        # Handle simple string enum variants
        elif "enum" in variant and variant.get("type") == "string":
            enum_vals = variant.get("enum", [])
            if enum_vals:
                enum_val = str(enum_vals[0])
                safe_name = enum_val.replace("-", "").replace("_", "").replace(" ", "").capitalize()
                
                # Ensure unique name
                original_safe_name = safe_name
                counter = 1
                while safe_name in used_variant_names:
                    safe_name = f"{original_safe_name}{counter}"
                    counter += 1
                used_variant_names.add(safe_name)
                
                # Track this enum variant for serializer (externally-tagged format)
                # NEAR sends enum variants as {"EnumValue": null} or similar
                variant_serializer_map.append((enum_val, safe_name, True, False))  # is_object=True, is_primitive=False
                
                code += f"    @Serializable\n"
                code += f'    @SerialName("{enum_val}")\n'
                code += f"    object {safe_name} : {kotlin_name}\n\n"
        
        # Handle single-property object (discriminated union pattern)
        elif variant.get("type") == "object" and "properties" in variant:
            props = variant.get("properties", {})
            
            # For internally-discriminated unions, generate as regular data class with all properties
            if discriminator_field and discriminator_field in props:
                # Get discriminator value for @SerialName
                discriminator_value = props[discriminator_field].get("enum", [""])[0]
                variant_class_name = to_kotlin_type_name(discriminator_value) if discriminator_value else f"Variant{idx}"
                
                # Ensure unique variant name
                original_variant_name = variant_class_name
                counter = 1
                while variant_class_name in used_variant_names:
                    variant_class_name = f"{original_variant_name}{counter}"
                    counter += 1
                used_variant_names.add(variant_class_name)
                
                # Remove the discriminator field from properties since kotlinx.serialization handles it
                variant_without_discriminator = variant.copy()
                props_copy = props.copy()
                del props_copy[discriminator_field]
                variant_without_discriminator["properties"] = props_copy
                
                # Generate data class without the discriminator property
                _, inline_props, nested_classes = generate_inline_data_class(variant_class_name, variant_without_discriminator, components)
                code += f"    @Serializable\n"
                code += f'    @SerialName("{discriminator_value}")\n'
                if inline_props.strip():  # Only add data class if there are properties
                    code += f"    data class {variant_class_name}({inline_props}) : {kotlin_name}\n\n"
                else:
                    # No properties besides discriminator - use object
                    code += f"    object {variant_class_name} : {kotlin_name}\n\n"
                if nested_classes:
                    code += nested_classes + "\n"
                continue  # Skip the rest of this section
            
            # Try to extract discriminator-based name first
            discriminator_name = extract_discriminator_name(variant)
            
            if len(props) == 1:
                prop_name = list(props.keys())[0]
                prop_schema = props[prop_name]
                
                # Use discriminator name if available, otherwise use property name
                if discriminator_name:
                    variant_class_name = discriminator_name
                else:
                    # Get the property type name to check for conflicts
                    prop_type = get_kotlin_type(prop_schema, components)
                    prop_type_base = prop_type.rstrip("?")  # Remove nullable marker
                    
                    # Use property name as base for variant class name
                    variant_class_name = to_kotlin_type_name(prop_name)
                    
                    # If the variant class name would shadow the property type, add "Request" suffix
                    # This prevents naming collisions like BlockId (variant) vs BlockId (type)
                    if variant_class_name == prop_type_base:
                        variant_class_name = f"{variant_class_name}Request"
                
                # Ensure unique variant name
                original_variant_name = variant_class_name
                counter = 1
                while variant_class_name in used_variant_names:
                    variant_class_name = f"{original_variant_name}{counter}"
                    counter += 1
                used_variant_names.add(variant_class_name)
                
                # For single-property oneOf variants, serialize the property directly at the root level
                # Use @SerialName on the data class itself (NOT on property) to indicate this is a content-based variant
                code += f"    @Serializable\n"
                code += f"    data class {variant_class_name}(\n"
                
                # Handle the wrapped property - inline it directly into the variant class
                # First, resolve $ref if present to check the actual schema structure
                resolved_prop_schema = prop_schema
                if "$ref" in prop_schema:
                    resolved_schema = resolve_ref_schema(prop_schema["$ref"], components)
                    if resolved_schema:
                        resolved_prop_schema = resolved_schema
                
                prop_type = get_kotlin_type(prop_schema, components)
                
                # Now check if the resolved schema has properties to inline
                if "properties" in resolved_prop_schema:
                    # Inline nested object properties directly
                    # This is externally-tagged format: {"PropertyName": {"nested": "fields"}}
                    variant_serializer_map.append((prop_name, variant_class_name, False, False))  # is_object=False, is_primitive=False
                    
                    _, inline_props, nested_classes = generate_inline_data_class(variant_class_name, resolved_prop_schema, components)
                    # Ensure proper indentation for the inlined properties
                    indented_props = "\n        ".join(line for line in inline_props.split("\n") if line.strip())
                    if indented_props:
                        code += f"        {indented_props}\n"
                    code += f"    ) : {kotlin_name}\n\n"
                    if nested_classes:
                        code += nested_classes + "\n"
                else:
                    # For simple types or type references - generate data class with @SerialName on the property
                    # This tells kotlinx.serialization to serialize as {"prop_name": value}
                    # Don't add to variant_serializer_map - let it use content-based serialization
                    
                    code += f"        @SerialName(\"{prop_name}\")\n"
                    code += f"        val {to_kotlin_property_name(prop_name)}: {prop_type}\n"
                    code += f"    ) : {kotlin_name}\n\n"
            else:
                # Multiple properties - try discriminator name, fallback to Variant{idx}
                if discriminator_name:
                    variant_class_name = discriminator_name
                else:
                    variant_class_name = f"Variant{idx}"
                
                # Ensure unique variant name
                original_variant_name = variant_class_name
                counter = 1
                while variant_class_name in used_variant_names:
                    variant_class_name = f"{original_variant_name}{counter}"
                    counter += 1
                used_variant_names.add(variant_class_name)
                
                _, inline_props, nested_classes = generate_inline_data_class(variant_class_name, variant, components)
                code += f"    @Serializable\n"
                code += f"    data class {variant_class_name}({inline_props}) : {kotlin_name}\n\n"
                if nested_classes:
                    code += nested_classes + "\n"
        
        # Handle reference variants
        elif "$ref" in variant:
            ref_name = resolve_ref_name(variant["$ref"])
            if ref_name:
                ref_kotlin_name = to_kotlin_type_name(ref_name)
                # Use @JvmInline value class for automatic unwrapping
                code += f"    @Serializable\n"
                code += f"    @JvmInline\n"
                code += f"    value class {ref_kotlin_name}Variant(\n"
                code += f"        val value: {ref_kotlin_name}\n"
                code += f"    ) : {kotlin_name}\n\n"
        
        # Handle primitive type variants
        elif variant.get("type") in ["string", "integer", "number", "boolean"]:
            var_type = variant.get("type")
            kotlin_type = get_kotlin_primitive_type(variant, components)
            type_name = var_type.capitalize()
            # Use @JvmInline value class for automatic unwrapping during serialization
            code += f"    @Serializable\n"
            code += f"    @JvmInline\n"
            code += f"    value class {type_name}Value(\n"
            code += f"        val value: {kotlin_type}\n"
            code += f"    ) : {kotlin_name}\n\n"
        
        else:
            # Fallback for very complex variants - generate with title if available
            variant_class_name = variant.get("title", f"Variant{idx}")
            variant_class_name = to_kotlin_type_name(variant_class_name)
            
            # Ensure unique variant name
            original_variant_name = variant_class_name
            counter = 1
            while variant_class_name in used_variant_names:
                variant_class_name = f"{original_variant_name}{counter}"
                counter += 1
            used_variant_names.add(variant_class_name)
            
            code += f"    @Serializable\n"
            code += f"    data class {variant_class_name}(\n"
            code += f"        val data: JsonElement\n"
            code += f"    ) : {kotlin_name}\n\n"
    
    code += "}\n\n"
    
    # Check if this is a content-based union (needs JsonContentPolymorphicSerializer)
    # This includes reference-only unions AND mixed unions with primitives
    # Also includes ANY sealed interface without a discriminator field
    is_content_based_union = False
    has_primitive_variants = False
    
    if not discriminator_field and len(variants) > 1:
        # Check if any variant is a primitive type (but not enum, since enums become objects)
        has_primitive_variants = any(
            v.get("type") in ["string", "integer", "number", "boolean"] and "enum" not in v
            for v in variants
        )
        # Check if any variant is a reference
        has_ref_variants = any("$ref" in v for v in variants)
        
        # Need content-based serializer if:
        # 1. We have refs or plain primitives (without external tags), OR
        # 2. We don't have variant_serializer_map (no externally-tagged serializer will be generated)
        is_content_based_union = has_ref_variants or has_primitive_variants or not variant_serializer_map
    
    # Check if all variants are allOf - these don't need custom serializers
    all_allof_variants = all("allOf" in v for v in variants)
    
    # Check if we'll generate externally-tagged serializer (all variants have wrappers)
    will_generate_externally_tagged = variant_serializer_map and len(variant_serializer_map) == len(variants)
    
    # Check if we have enum string variants - these need special handling
    has_enum_string_variants = any(
        "enum" in v and v.get("type") == "string"
        for v in variants
    )
    
    # Generate custom serializer for ALL sealed interfaces without discriminators
    # These unions have no discriminator, so we detect variant type by inspecting JSON content
    # Generate this BEFORE checking externally-tagged serializers
    # BUT skip if all variants are allOf (just regular data classes) OR if externally-tagged serializer will be generated
    if not discriminator_field and len(variants) > 1 and not all_allof_variants and not will_generate_externally_tagged:
        will_generate_custom_serializer = True
        code += f"// Custom serializer for {kotlin_name} to handle content-based polymorphism\n"
        
        # If we have enum string variants, we need a full KSerializer (not just JsonContentPolymorphicSerializer)
        # because enum string objects need to serialize as plain strings
        if has_enum_string_variants:
            code += f"object {kotlin_name}Serializer : KSerializer<{kotlin_name}> {{\n"
            code += f"    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(\"{kotlin_name}\")\n\n"
            
            # Generate serialize method
            code += f"    override fun serialize(encoder: Encoder, value: {kotlin_name}) {{\n"
            code += f"        val output = encoder as? JsonEncoder ?: throw SerializationException(\"This serializer only works with JSON\")\n"
            code += f"        when (value) {{\n"
            
            # Handle enum string objects - serialize as plain string
            for idx, variant in enumerate(variants):
                if "enum" in variant and variant.get("type") == "string":
                    enum_vals = variant.get("enum", [])
                    if enum_vals:
                        enum_val = str(enum_vals[0])
                        safe_name = enum_val.replace("-", "").replace("_", "").replace(" ", "").capitalize()
                        code += f'            is {kotlin_name}.{safe_name} -> output.encodeJsonElement(JsonPrimitive("{enum_val}"))\n'
            
            # Handle other variants - use their default serializers
            for idx, variant in enumerate(variants):
                if "enum" in variant and variant.get("type") == "string":
                    continue  # Already handled above
                elif variant.get("type") == "object" and "properties" in variant:
                    props = variant.get("properties", {})
                    if props and len(props) == 1:
                        prop_name = list(props.keys())[0]
                        discriminator_name = extract_discriminator_name(variant)
                        prop_schema = props[prop_name]
                        prop_type = get_kotlin_type(prop_schema, components)
                        prop_type_base = prop_type.rstrip("?")
                        
                        if discriminator_name:
                            variant_class_name = discriminator_name
                        else:
                            variant_class_name = to_kotlin_type_name(prop_name)
                            if variant_class_name == prop_type_base:
                                variant_class_name = f"{variant_class_name}Request"
                        
                        code += f'            is {kotlin_name}.{variant_class_name} -> output.encodeSerializableValue({kotlin_name}.{variant_class_name}.serializer(), value)\n'
            
            code += f"        }}\n"
            code += f"    }}\n\n"
            
            # Generate deserialize method
            code += f"    override fun deserialize(decoder: Decoder): {kotlin_name} {{\n"
            code += f"        val input = decoder as? JsonDecoder ?: throw SerializationException(\"This serializer only works with JSON\")\n"
            code += f"        val element = input.decodeJsonElement()\n"
            code += f"        return when {{\n"
            
            # Check for enum string values first
            for idx, variant in enumerate(variants):
                if "enum" in variant and variant.get("type") == "string":
                    enum_vals = variant.get("enum", [])
                    if enum_vals:
                        enum_val = str(enum_vals[0])
                        safe_name = enum_val.replace("-", "").replace("_", "").replace(" ", "").capitalize()
                        code += f'            element is JsonPrimitive && element.content == "{enum_val}" -> {kotlin_name}.{safe_name}\n'
            
            # Then check for object variants by their properties
            for idx, variant in enumerate(variants):
                if "enum" in variant and variant.get("type") == "string":
                    continue  # Already handled above
                elif variant.get("type") == "object" and "properties" in variant:
                    props = variant.get("properties", {})
                    if props and len(props) == 1:
                        prop_name = list(props.keys())[0]
                        discriminator_name = extract_discriminator_name(variant)
                        prop_schema = props[prop_name]
                        prop_type = get_kotlin_type(prop_schema, components)
                        prop_type_base = prop_type.rstrip("?")
                        
                        if discriminator_name:
                            variant_class_name = discriminator_name
                        else:
                            variant_class_name = to_kotlin_type_name(prop_name)
                            if variant_class_name == prop_type_base:
                                variant_class_name = f"{variant_class_name}Request"
                        
                        code += f'            "{prop_name}" in element.jsonObject -> input.json.decodeFromJsonElement({kotlin_name}.{variant_class_name}.serializer(), element)\n'
            
            code += f'            else -> throw SerializationException("Unknown variant in {kotlin_name}: ${{element}}")\n'
            code += f"        }}\n"
            code += f"    }}\n"
            code += f"}}\n\n"
        else:
            # Use JsonContentPolymorphicSerializer for types without enum strings
            code += f"object {kotlin_name}Serializer : JsonContentPolymorphicSerializer<{kotlin_name}>({kotlin_name}::class) {{\n"
            code += f"    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<{kotlin_name}> {{\n"
            code += f"        return when {{\n"
            
            # Generate when branches based on variant type
            for idx, variant in enumerate(variants):
                # Handle primitive type variants first (but skip enum variants as they're generated as objects)
                if variant.get("type") in ["string", "integer", "number", "boolean"] and "enum" not in variant:
                    var_type = variant.get("type")
                    type_name = var_type.capitalize()
                    
                    if var_type == "integer":
                        code += f'            element is JsonPrimitive && element.longOrNull != null -> {kotlin_name}.{type_name}Value.serializer()\n'
                    elif var_type == "number":
                        code += f'            element is JsonPrimitive && element.doubleOrNull != null -> {kotlin_name}.{type_name}Value.serializer()\n'
                    elif var_type == "boolean":
                        code += f'            element is JsonPrimitive && element.booleanOrNull != null -> {kotlin_name}.{type_name}Value.serializer()\n'
                    elif var_type == "string":
                        # String is tricky - check if it's not an object and fallback to string
                        code += f'            element is JsonPrimitive && element.isString -> {kotlin_name}.{type_name}Value.serializer()\n'
                
                # Handle enum variants (generated as objects)
                elif "enum" in variant and variant.get("type") == "string":
                    enum_vals = variant.get("enum", [])
                    if enum_vals:
                        enum_val = str(enum_vals[0])
                        safe_name = enum_val.replace("-", "").replace("_", "").replace(" ", "").capitalize()
                        # Note: we assume the variant name was generated uniquely in the variant generation section
                        # For content-based serializers, enum objects are singleton objects, so check for specific marker
                        # Since these are objects (not data classes), we can't easily detect them by properties
                        # For now, we skip enum objects in content-based serializers as they should be handled
                        # via externally-tagged serializers instead
                        pass  # Skip enum objects - they should use externally-tagged format
                
                # Handle object variants with properties - check for unique property keys
                elif variant.get("type") == "object" and "properties" in variant:
                    props = variant.get("properties", {})
                    if props:
                        # Get the variant class name - we need to match what was generated above
                        discriminator_name = extract_discriminator_name(variant)
                        if len(props) == 1:
                            prop_name = list(props.keys())[0]
                            prop_schema = props[prop_name]
                            prop_type = get_kotlin_type(prop_schema, components)
                            prop_type_base = prop_type.rstrip("?")
                            variant_class_name = to_kotlin_type_name(prop_name) if not discriminator_name else discriminator_name
                            if variant_class_name == prop_type_base:
                                variant_class_name = f"{variant_class_name}Request"
                        elif discriminator_name:
                            variant_class_name = discriminator_name
                        else:
                            variant_class_name = f"Variant{idx}"
                        
                        # Check for the specific property keys to identify this variant
                        prop_names = list(props.keys())
                        if len(prop_names) == 1:
                            code += f'            "{prop_names[0]}" in element.jsonObject -> {kotlin_name}.{variant_class_name}.serializer()\n'
                        else:
                            # Check for first 2 properties
                            conditions = " && ".join([f'"{prop}" in element.jsonObject' for prop in prop_names[:2]])
                            code += f'            {conditions} -> {kotlin_name}.{variant_class_name}.serializer()\n'
                
                # Handle reference variants
                elif "$ref" in variant:
                    ref_name = resolve_ref_name(variant["$ref"])
                    if ref_name:
                        ref_kotlin_name = to_kotlin_type_name(ref_name)
                        variant_class_name = f"{ref_kotlin_name}Variant"
                        
                        # Get the schema for this ref to find unique fields
                        ref_schema = components.get(ref_name, {})
                        
                        # Check if the referenced type is a primitive (string, etc)
                        ref_type = ref_schema.get("type")
                        if ref_type == "string":
                            # For string refs like CryptoHash, check if it's a string primitive
                            code += f'            element is JsonPrimitive && element.isString -> {kotlin_name}.{variant_class_name}.serializer()\n'
                        elif ref_type == "object" or "properties" in ref_schema:
                            # For object refs, check for unique fields
                            props = ref_schema.get("properties", {})
                            if props:
                                # Use first few properties as discriminators
                                prop_names = list(props.keys())[:2]  # Check first 2 properties
                                conditions = " && ".join([f'"{prop}" in element.jsonObject' for prop in prop_names])
                                code += f'            {conditions} -> {kotlin_name}.{variant_class_name}.serializer()\n'
                            else:
                                # Object with no properties
                                code += f'            element is JsonObject -> {kotlin_name}.{variant_class_name}.serializer()\n'
            
            code += f'            else -> throw SerializationException("Unknown variant in {kotlin_name}: type=${{element::class.simpleName}}")\n'
            code += f"        }}\n"
            code += f"    }}\n"
            code += f"}}\n\n"
    
    # Generate custom serializer for externally-tagged unions
    # Only generate if ALL variants are externally-tagged (100% match)
    # This ensures the when statement in serialize() will be exhaustive
    elif variant_serializer_map and len(variant_serializer_map) == len(variants):
        code += f"// Custom serializer for {kotlin_name} to handle NEAR's externally-tagged union format\n"
        code += f"object {kotlin_name}Serializer : KSerializer<{kotlin_name}> {{\n"
        code += f"    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(\"{kotlin_name}\")\n\n"
        
        code += f"    override fun serialize(encoder: Encoder, value: {kotlin_name}) {{\n"
        code += f"        val output = encoder as? JsonEncoder ?: throw SerializationException(\"This serializer only works with JSON\")\n"
        code += f"        when (value) {{\n"
        
        # Generate when branches for serialization
        for wrapper_key, variant_class, is_object, is_primitive in variant_serializer_map:
            if is_object:
                # For object (singleton), encode as {"key": null}
                code += f'            is {kotlin_name}.{variant_class} -> output.encodeJsonElement(buildJsonObject {{ put("{wrapper_key}", JsonNull) }})\n'
            else:
                # For data classes, serialize them directly - kotlinx.serialization will use @SerialName annotations
                # This ensures {"prop_name": value} format without extra nesting
                code += f'            is {kotlin_name}.{variant_class} -> output.encodeSerializableValue({kotlin_name}.{variant_class}.serializer(), value)\n'
        
        code += f"        }}\n"
        code += f"    }}\n\n"
        
        code += f"    override fun deserialize(decoder: Decoder): {kotlin_name} {{\n"
        code += f"        val input = decoder as? JsonDecoder ?: throw SerializationException(\"This serializer only works with JSON\")\n"
        code += f"        val element = input.decodeJsonElement().jsonObject\n"
        code += f"        return when {{\n"
        
        # Generate when branches for deserialization
        for wrapper_key, variant_class, is_object, is_primitive in variant_serializer_map:
            if is_object:
                # For object (singleton), just return the object itself
                code += f'            "{wrapper_key}" in element -> {kotlin_name}.{variant_class}\n'
            else:
                # For data classes, deserialize from the nested object inside the wrapper key
                # Extract element["{wrapper_key}"] to get the actual data
                code += f'            "{wrapper_key}" in element -> input.json.decodeFromJsonElement({kotlin_name}.{variant_class}.serializer(), element["{wrapper_key}"]!!)\n'
        
        code += f"            else -> throw SerializationException(\"Unknown variant in {kotlin_name}: ${{element.keys}}\")\n"
        code += f"        }}\n"
        code += f"    }}\n"
        code += f"}}\n\n"
    
    return code

def generate_kotlin_for_schema(name: str, schema: Dict[str, Any], components: Dict[str, Any], generated_types: Set[str]) -> str:
    """Generate Kotlin code for a schema"""
    
    # Skip simple references
    if "$ref" in schema and len(schema) == 1:
        return ""
    
    # Generate enum
    if "enum" in schema:
        return generate_kotlin_enum(name, schema)
    
    # Check if this has union types (oneOf/anyOf)
    has_union = "oneOf" in schema or "anyOf" in schema
    has_properties = "properties" in schema
    is_object = schema.get("type") == "object"
    
    # If has union types, generate sealed interface with type-safe variants
    if has_union:
        return generate_kotlin_sealed_interface(name, schema, components, generated_types)
    
    # Handle allOf - need to check if it resolves to a primitive type or object
    if "allOf" in schema:
        merged = merge_allof(schema["allOf"], components)
        merged_type = merged.get("type")
        
        # If allOf resolves to a primitive type, generate typealias
        if merged_type in ["string", "integer", "number", "boolean"]:
            base_type = get_kotlin_primitive_type(merged, components)
            kotlin_name = to_kotlin_type_name(name)
            if not register_generated_type(kotlin_name, generated_types):
                return ""
            description = schema.get("description", "").strip()
            if description:
                first_line = description.split("\n")[0]
                if len(first_line) > 80:
                    first_line = first_line[:77] + "..."
                return f"/**\n * {first_line}\n */\ntypealias {kotlin_name} = {base_type}\n\n"
            return f"typealias {kotlin_name} = {base_type}\n\n"
        
        # Otherwise treat as object/data class
        return generate_kotlin_data_class(name, schema, components, generated_types)
    
    # Generate data class for objects with properties
    if has_properties or is_object:
        return generate_kotlin_data_class(name, schema, components, generated_types)
    
    # Generate typealias for primitive types that are given names in the schema
    schema_type = schema.get("type")
    
    if schema_type in ["string", "integer", "number", "boolean"]:
        kotlin_name = to_kotlin_type_name(name)
        if not register_generated_type(kotlin_name, generated_types):
            return ""
        
        # Determine the Kotlin type
        if schema_type == "string":
            kotlin_type = "String"
        elif schema_type == "integer":
            # Use Long for uint64/int64, Int for int32
            if schema.get("format") == "uint64":
                kotlin_type = "Long"
            elif schema.get("format") == "int64":
                kotlin_type = "Long"
            elif schema.get("format") == "int32":
                kotlin_type = "Int"
            else:
                kotlin_type = "Long"  # Default to Long for unspecified integers
        elif schema_type == "number":
            if schema.get("format") == "float":
                kotlin_type = "Float"
            else:
                kotlin_type = "Double"
        elif schema_type == "boolean":
            kotlin_type = "Boolean"
        else:
            kotlin_type = "String"
        
        # Generate with documentation if available
        description = schema.get("description", "").strip()
        if description:
            # Truncate long descriptions
            first_line = description.split("\n")[0]
            if len(first_line) > 80:
                first_line = first_line[:77] + "..."
            
            doc = f"/**\n * {first_line}\n */\n"
            return f"{doc}typealias {kotlin_name} = {kotlin_type}\n\n"
        else:
            return f"typealias {kotlin_name} = {kotlin_type}\n\n"
    
    # Handle array types
    if schema_type == "array":
        kotlin_name = to_kotlin_type_name(name)
        if not register_generated_type(kotlin_name, generated_types):
            return ""
        
        items = schema.get("items", {})
        items_type = get_kotlin_type(items, components)
        
        description = schema.get("description", "").strip()
        if description:
            first_line = description.split("\n")[0]
            if len(first_line) > 80:
                first_line = first_line[:77] + "..."
            return f"/**\n * {first_line}\n */\ntypealias {kotlin_name} = List<{items_type}>\n\n"
        return f"typealias {kotlin_name} = List<{items_type}>\n\n"
    
    return ""

# --- Methods Generation ---

def generate_methods_code(openapi: Dict[str, Any], components_schemas: Dict[str, Any]) -> str:
    """Generate Methods.kt with extension functions for all RPC methods"""
    
    header = """package org.near.jsonrpc.client

import kotlinx.serialization.builtins.*
import org.near.jsonrpc.types.*

/**
 * Extension functions for type-safe access to NEAR JSON-RPC methods.
 */

"""
    
    methods_code = header
    paths = openapi.get("paths", {})
    
    # Sort methods alphabetically for consistency
    sorted_paths = sorted(paths.items())
    
    for path, path_item in sorted_paths:
        if "post" not in path_item:
            continue
            
        post_op = path_item["post"]
        operation_id = post_op.get("operationId", "")
        description = post_op.get("description", "")
        
        if not operation_id:
            continue
        
        # Get request and response types from schemas
        request_schema_ref = None
        response_schema_ref = None
        
        # Extract request schema
        request_body = post_op.get("requestBody", {})
        if request_body:
            content = request_body.get("content", {})
            json_content = content.get("application/json", {})
            schema = json_content.get("schema", {})
            if "$ref" in schema:
                request_schema_ref = schema["$ref"]
        
        # Extract response schema
        responses = post_op.get("responses", {})
        response_200 = responses.get("200", {})
        if response_200:
            content = response_200.get("content", {})
            json_content = content.get("application/json", {})
            schema = json_content.get("schema", {})
            if "$ref" in schema:
                response_schema_ref = schema["$ref"]
        
        if not request_schema_ref or not response_schema_ref:
            continue
        
        # Extract type names
        request_type_name = resolve_ref_name(request_schema_ref)
        response_type_name = resolve_ref_name(response_schema_ref)
        
        if not request_type_name or not response_type_name:
            continue
        
        # Convert to Kotlin type names
        request_kotlin_type = to_kotlin_type_name(request_type_name)
        response_kotlin_type = to_kotlin_type_name(response_type_name)
        
        # Generate method name (camelCase)
        method_name = to_kotlin_property_name(operation_id)
        
        # Get the params type from the request schema
        request_schema = components_schemas.get(request_type_name, {})
        params_type = "JsonElement"
        
        # Try to extract params type from request schema properties
        if "properties" in request_schema:
            props = request_schema["properties"]
            if "params" in props:
                params_schema = props["params"]
                params_type = get_kotlin_type(params_schema, components_schemas)
        
        # Extract the result type from the response schema
        # Response schema is typically JsonRpcResponse_for_ResultType_and_ErrorType
        # We need to extract the actual result type
        response_schema = components_schemas.get(response_type_name, {})
        result_type = "JsonElement"
        
        # Response schemas typically have oneOf with result and error variants
        if "oneOf" in response_schema:
            # Find the variant with "result" property
            for variant in response_schema["oneOf"]:
                if "properties" in variant and "result" in variant["properties"]:
                    result_schema = variant["properties"]["result"]
                    result_type = get_kotlin_type(result_schema, components_schemas)
                    break
        # Fallback: Try to extract result type from response schema properties directly
        elif "properties" in response_schema:
            props = response_schema["properties"]
            if "result" in props:
                result_schema = props["result"]
                result_type = get_kotlin_type(result_schema, components_schemas)
        
        # Generate KDoc
        methods_code += "/**\n"
        if description:
            desc_lines = description.strip().split("\n")
            first_line = desc_lines[0] if desc_lines else ""
            if first_line:
                if len(first_line) > 100:
                    first_line = first_line[:97] + "..."
                methods_code += f" * {first_line}\n"
        methods_code += " */\n"
        
        # Generate extension function
        methods_code += f"suspend fun NearRpcClient.{method_name}(\n"
        methods_code += f"    params: {params_type}\n"
        methods_code += f"): {result_type} = call(\n"
        methods_code += f"    method = \"{operation_id}\",\n"
        methods_code += f"    params = params,\n"
        
        # Determine the serializer for params
        if params_type == "JsonElement":
            methods_code += f"    paramsSerializer = JsonElement.serializer(),\n"
        elif params_type.startswith("List<"):
            inner_type = params_type[5:-1]  # Extract type from List<Type>
            if inner_type == "String":
                methods_code += f"    paramsSerializer = ListSerializer(String.serializer()),\n"
            else:
                methods_code += f"    paramsSerializer = ListSerializer({inner_type}.serializer()),\n"
        elif params_type.endswith("?"):
            base_type = params_type[:-1]
            methods_code += f"    paramsSerializer = {base_type}.serializer(),\n"
        else:
            methods_code += f"    paramsSerializer = {params_type}.serializer(),\n"
        
        # Determine the serializer for result
        if result_type == "JsonElement":
            methods_code += f"    resultSerializer = JsonElement.serializer()\n"
        elif result_type.startswith("List<"):
            inner_type = result_type[5:-1]
            if inner_type == "String":
                methods_code += f"    resultSerializer = ListSerializer(String.serializer())\n"
            else:
                methods_code += f"    resultSerializer = ListSerializer({inner_type}.serializer())\n"
        elif result_type.endswith("?"):
            base_type = result_type[:-1]
            methods_code += f"    resultSerializer = {base_type}.serializer()\n"
        else:
            methods_code += f"    resultSerializer = {result_type}.serializer()\n"
        
        methods_code += ")\n\n"
    
    return methods_code


def main():
    """Main function to generate Kotlin types from OpenAPI spec"""
    print(f"Loading OpenAPI specification from {OPENAPI_PATH}...")
    openapi = load_openapi()
    
    components_schemas = openapi.get("components", {}).get("schemas", {})
    if not components_schemas:
        print("No schemas found in OpenAPI specification")
        return
    
    print(f"Found {len(components_schemas)} schemas")
    
    # Generate Kotlin code
    kotlin_code = HEADER_CODE
    
    generated_types = set()
    custom_serializers = []  # Track sealed interfaces with custom serializers
    
    # Sort schemas by complexity
    def schema_complexity(item):
        name, schema = item
        if "$ref" in schema and len(schema) == 1:
            return 0
        elif schema.get("type") in ["string", "integer", "number", "boolean"]:
            return 1
        elif "enum" in schema:
            return 2
        elif schema.get("type") == "array":
            return 3
        elif "oneOf" in schema or "anyOf" in schema:
            return 4
        elif "allOf" in schema:
            return 5
        else:
            return 6
    
    sorted_schemas = sorted(components_schemas.items(), key=schema_complexity)
    
    for name, schema in sorted_schemas:
        code = generate_kotlin_for_schema(name, schema, components_schemas, generated_types)
        if code:
            kotlin_code += code
            
            # Track if this is a sealed interface with a custom serializer
            kotlin_name = to_kotlin_type_name(name)
            if ("oneOf" in schema or "anyOf" in schema) and "@Serializable(with =" in code:
                custom_serializers.append(kotlin_name)
    
    kotlin_code += "\n/**\n"
    kotlin_code += " * SerializersModule for NEAR's externally-tagged unions.\n"
    kotlin_code += " */\n"
    kotlin_code += "val nearSerializersModule = SerializersModule {\n"
    
    for type_name in sorted(custom_serializers):
        kotlin_code += f"    polymorphic({type_name}::class) {{ defaultDeserializer {{ {type_name}Serializer }} }}\n"
    
    kotlin_code += "}\n"
    
    # Write Types.kt
    output_dir = os.path.dirname(os.path.abspath(OUTPUT_TYPES_PATH))
    os.makedirs(output_dir, exist_ok=True)
    
    print(f"Writing Kotlin types to {OUTPUT_TYPES_PATH}...")
    with open(OUTPUT_TYPES_PATH, "w", encoding="utf-8") as f:
        f.write(kotlin_code)
    
    print(f"Successfully generated {len(generated_types)} Kotlin types")
    print(f"Output written to: {OUTPUT_TYPES_PATH}")
    
    # Generate and write Methods.kt
    print(f"\nGenerating RPC methods...")
    methods_code = generate_methods_code(openapi, components_schemas)
    
    methods_dir = os.path.dirname(os.path.abspath(OUTPUT_METHODS_PATH))
    os.makedirs(methods_dir, exist_ok=True)
    
    print(f"Writing methods to {OUTPUT_METHODS_PATH}...")
    with open(OUTPUT_METHODS_PATH, "w", encoding="utf-8") as f:
        f.write(methods_code)
    
    # Count methods
    method_count = len([p for p in openapi.get("paths", {}).values() if "post" in p])
    print(f"Successfully generated {method_count} RPC methods")
    print(f"Output written to: {OUTPUT_METHODS_PATH}")
    
    print(f"\n✅ Code generation complete!")

if __name__ == "__main__":
    main()
