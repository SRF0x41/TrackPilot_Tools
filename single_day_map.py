import os
import folium
from PIL import Image
import io

rel_path = "lrt/2025_11_3"
denver_lat, denver_lon = 39.7392, -104.9903
denver = folium.Map(location=[denver_lat, denver_lon], zoom_start=11)

for dirpath, dirnames, filenames in os.walk(rel_path):
    for file in filenames:
        file_path = os.path.join(dirpath, file)
        try:
            with open(file_path, 'r') as f:
                for line in f:
                    data_line = line.strip().split(',')
                    if len(data_line) < 3:
                        continue  # skip junk lines

                    lat = float(data_line[1])
                    lon = float(data_line[2])

                    # Print for debugging
                    print(f"Lat {lat} Lon {lon}")

                    # Small red dot, no marker pin
                    folium.CircleMarker(
                        location=[lat, lon],
                        radius=.3,          # tiny dot
                        color='red',
                        fill=True,
                        fill_opacity=0.8
                    ).add_to(denver)
        except Exception as e:
            print(f"Error reading {file_path}: {e}")

print("Saving to HTML...")
denver.save("denver.html")

print("Saving to PNG...")
try:
    img_data = denver._to_png(5)  # Give tiles time to load
    img = Image.open(io.BytesIO(img_data))
    png_file = "denver_map.png"
    img.save(png_file)
    print(f"✅ PNG saved to {png_file}")
except Exception as e:
    print("❌ Could not export directly to PNG:", e)
    print("Try taking a screenshot of the HTML map manually or using selenium.")
