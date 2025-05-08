import os
import sys
import re

def parse_time_to_seconds(time_str):
    """Convert a time string in the format '0m24.239s' to seconds."""
    match = re.match(r'(\d+)m([\d.]+)s', time_str)
    if match:
        minutes = int(match.group(1))
        seconds = float(match.group(2))
        return minutes * 60 + seconds
    else:
        return None

def main():
    directory = sys.argv[1]
    log_files = [f for f in os.listdir(directory) if f.endswith('.log')]
    log_files = sorted(log_files)

    for file in log_files:
        total_time = 0
        count = 0
        file_path = os.path.join(directory, file)
        with open(file_path, 'r') as f:
            for line in f:
                # Check if the line matches the expected pattern
                if line.startswith('real'):
                    parts = line.strip().split('\t')
                    if len(parts) == 2:
                        time_in_seconds = parse_time_to_seconds(parts[1])
                        if time_in_seconds is not None:
                            total_time += time_in_seconds
                            count += 1

        if count > 0:
            average_time = total_time / count
            print(f"{file} - Average time in seconds: {average_time:.3f} for {count} runs")
        else:
            print(f"{file} - No valid entries found.")

if __name__ == '__main__':
    main()
