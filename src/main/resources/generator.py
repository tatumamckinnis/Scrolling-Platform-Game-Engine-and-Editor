import random
import uuid

def generate_object_node(start_x=100, start_y=300, number_of_objects=10):
  """
          <object id="111" coordinates="(100,400)" uid="e816f04c-3047-4e30-9e20-2e601a99dde8"/>
  Generates an XML object node with multiple objects.

  <object id="12" coordinates="(300,300)(500,300)"
          uid="e0373626-9e90-4ed3-9863-39e287926802,7f34d811-6998-452e-9b8d-49b44f4dca41"/>
  Each object is represented by two coordinate pairs: a starting coordinate
  and an ending coordinate (where the ending coordinate is start_x + offset,
  with offset randomly selected between 200 and 300). Each object is also
  assigned a new UUID.

  The resulting XML <object> node will have:
    - An id attribute set to 14.
    - A coordinates attribute containing comma-separated coordinate pairs.
    - A uid attribute containing comma-separated UUIDs (one per object).

  Parameters:
    start_x (int): The starting x-coordinate for the first object.
    start_y (int): The y-coordinate (constant for all objects).
    number_of_objects (int): The number of objects to generate.

  Returns:
    str: An XML string representing the object node.
  """
  coordinate_parts = []
  uuid_parts = []
  current_x = start_x

  for _ in range(number_of_objects):
    # Random offset between 200 and 300 pixels.
    offset = random.randint(200, 300)
    start_coord = f"({current_x},{start_y})"
    end_coord = f"({current_x + offset},{start_y})"
    # Append both coordinate pairs for this object.
    coordinate_parts.append(end_coord)
    # Generate a new UUID for this object.
    uuid_parts.append(str(uuid.uuid4()))
    # Update current_x for the next object.
    current_x += offset

  # Create comma separated lists.
  coordinates_str = ", ".join(coordinate_parts)
  uuid_str = ", ".join(uuid_parts)

  # Construct the XML node.
  xml_node = f'<object id="12" coordinates="{coordinates_str}" uid="{uuid_str}" />'
  return xml_node

if __name__ == "__main__":
  xml_output = generate_object_node()
  print(xml_output)
