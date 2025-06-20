#!/bin/bash

# Hostname
hostname_val=$(hostname)
echo "hostname=$hostname_val"

# IP Address (cross-platform)
if command -v hostname &> /dev/null && hostname | grep -qE '\.'; then
  ip_address=$(ipconfig getifaddr en0 2>/dev/null || ifconfig en0 | grep "inet " | awk '{print $2}' || echo "Unavailable")
else
  ip_address=$(hostname -I 2>/dev/null | awk '{print $1}' || echo "Unavailable")
fi
echo "ipAddress=$ip_address"

# RAM (Linux only)
if command -v free &> /dev/null; then
  total_ram=$(free -h | awk '/Mem:/ {print $2}')
  available_ram=$(free -h | awk '/Mem:/ {print $7}')
else
  total_ram="Unavailable"
  available_ram="Unavailable"
fi
echo "memoryTotal=$total_ram"
echo "memoryAvailable=$available_ram"

# OS Release
if [ -f /etc/os-release ]; then
  os_release=$(grep PRETTY_NAME /etc/os-release | cut -d= -f2- | tr -d '"')
elif [ "$(uname)" == "Darwin" ]; then
  os_release=$(sw_vers -productName)$(sw_vers -productVersion)
else
  os_release="Unavailable"
fi
echo "operatingSystem=$os_release"

# Disk Space
disk_total=$(df -h / | awk 'NR==2 {print $2}')
disk_free=$(df -h / | awk 'NR==2 {print $4}')
echo "diskTotal=$disk_total"
echo "diskFree=$disk_free"
