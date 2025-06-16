#!/bin/bash

# Function to safely get information or return error message
safe_get_info() {
  local cmd="$1"
  local info_name="$2"
  local result

  if result=$($cmd 2>/dev/null); then
    echo "$info_name=$result"
  else
    echo "$info_name=Couldn't retrieve $info_name information"
  fi
}

# --- SYSTEM INFORMATION ---

# Hostname
safe_get_info "hostname" "hostname"

# OS
if [ -f /etc/os-release ]; then
  os=$(grep PRETTY_NAME /etc/os-release | cut -d= -f2 | tr -d '"')
else
  os=$(uname -s)
fi
echo "operatingSystem=${os:-Couldn't retrieve OS information}"

# Kernel version
safe_get_info "uname -r" "kernelVersion"

# Architecture
safe_get_info "uname -m" "architecture"

# Uptime
uptime_value=$(uptime 2>/dev/null | sed 's/^.*up //' | sed 's/,  *[0-9]* user.*$//')
echo "uptime=${uptime_value:-Couldn't retrieve uptime information}"

# Timezone
timezone=$(timedatectl 2>/dev/null | grep 'Time zone' | awk '{print $3, $4}' | tr -d ',' || \
          cat /etc/timezone 2>/dev/null || \
          date +%Z 2>/dev/null)
echo "timezone=${timezone:-Couldn't retrieve timezone information}"

# Boot Time
boot_time=$(who -b 2>/dev/null | awk '{print $3, $4}' || \
            uptime -s 2>/dev/null || \
            date -d "$(awk '{print $1}' /proc/uptime) seconds ago" +"%F %T" 2>/dev/null)
echo "bootTime=${boot_time:-Couldn't retrieve boot time information}"

# Kernel Modules Count
safe_get_info "lsmod | wc -l" "kernelModulesCount"

# Installed Packages Count
if command -v rpm &> /dev/null; then
  safe_get_info "rpm -qa | wc -l" "packageCount"
elif command -v dpkg &> /dev/null; then
  safe_get_info "dpkg -l | wc -l" "packageCount"
else
  echo "packageCount=Couldn't retrieve package count information"
fi

# CPU Info
cpu_model=$(lscpu 2>/dev/null | grep "Model name" | sed 's/.*: *//' || \
            cat /proc/cpuinfo 2>/dev/null | grep 'model name' | head -1 | cut -d: -f2 | sed 's/^ *//')
echo "cpuModel=${cpu_model:-Couldn't retrieve CPU model information}"

cpu_cores=$(nproc 2>/dev/null || \
            grep -c ^processor /proc/cpuinfo 2>/dev/null || \
            echo "Couldn't retrieve CPU cores information")
echo "cpuCores=$cpu_cores"

# Memory Info
total_mem=$(free -h 2>/dev/null | grep Mem | awk '{print $2}' || \
            grep MemTotal /proc/meminfo 2>/dev/null | awk '{print $2 $3}')
used_mem=$(free -h 2>/dev/null | grep Mem | awk '{print $3}' || \
           grep MemAvailable /proc/meminfo 2>/dev/null | awk '{print $2 $3}')
echo "memoryTotal=${total_mem:-Couldn't retrieve total memory information}"
echo "memoryUsed=${used_mem:-Couldn't retrieve used memory information}"

# Load average
load_avg=$(cat /proc/loadavg 2>/dev/null | awk '{print $1", "$2", "$3}' || \
           uptime 2>/dev/null | grep -o 'load average.*' | cut -d: -f2)
echo "loadAverage=${load_avg:-Couldn't retrieve load average information}"

# Disk Info
disk_space=$(df -h / 2>/dev/null | awk 'NR==2{print $2}')
echo "diskSpace=${disk_space:-Couldn't retrieve disk space information}"

# IP address
ip_address=$(hostname -I 2>/dev/null | awk '{print $1}' || \
             ip a 2>/dev/null | grep 'inet ' | grep -v '127.0.0.1' | head -1 | awk '{print $2}' | cut -d/ -f1)
echo "ipAddress=${ip_address:-Couldn't retrieve IP address information}"

# Default gateway
default_gateway=$(ip route 2>/dev/null | grep default | awk '{print $3}' || \
                  route -n 2>/dev/null | grep 'UG[ \t]' | awk '{print $2}')
echo "defaultGateway=${default_gateway:-Couldn't retrieve default gateway information}"

# DNS servers
dns_servers=$(grep "nameserver" /etc/resolv.conf 2>/dev/null | awk '{print $2}' | paste -sd "," - || \
              systemd-resolve --status 2>/dev/null | grep 'DNS Servers' | awk '{print $3}' | paste -sd "," -)
echo "dnsServers=${dns_servers:-Couldn't retrieve DNS servers information}"

# MAC Address
mac_address=$(ip link show eth0 2>/dev/null | awk '/ether/ {print $2}' || \
              ifconfig eth0 2>/dev/null | grep 'ether' | awk '{print $2}')
echo "macAddress=${mac_address:-Couldn't retrieve MAC address information}"

# Network Interfaces
network_interfaces=$(ip -o link show 2>/dev/null | awk -F': ' '{print $2}' | paste -sd ',' - || \
                     ifconfig -a 2>/dev/null | grep '^[a-z]' | awk '{print $1}' | paste -sd ',' -)
echo "networkInterfaces=${network_interfaces:-Couldn't retrieve network interfaces information}"

# Public IP
public_ip=$(curl -s https://ipinfo.io/ip 2>/dev/null || \
            curl -s ifconfig.me 2>/dev/null || \
            echo "Couldn't retrieve public IP information")
echo "publicIp=$public_ip"

# Firewall
if command -v ufw &> /dev/null; then
  ufw_status=$(ufw status 2>/dev/null | grep -q "Status: active" && echo true || echo false)
elif command -v firewall-cmd &> /dev/null; then
  ufw_status=$(firewall-cmd --state 2>/dev/null | grep -q "running" && echo true || echo false)
else
  ufw_status=false
fi
echo "firewallStatus=$ufw_status"

# SSH status
ssh_enabled=$(systemctl is-active ssh 2>/dev/null || \
              systemctl is-active sshd 2>/dev/null || \
              service ssh status 2>/dev/null | grep -q 'running' && echo active || echo inactive)
echo "sshEnabled=$([ "$ssh_enabled" = "active" ] && echo true || echo false)"

# Open Ports
open_ports=$(ss -tuln 2>/dev/null | awk 'NR>1 {print $5}' | grep -oE '[0-9]+$' | sort -n | uniq | paste -sd "," - || \
             netstat -tuln 2>/dev/null | awk '/^tcp|^udp/ {print $4}' | rev | cut -d: -f1 | rev | sort -n | uniq | paste -sd "," -)
echo "openPorts=${open_ports:-Couldn't retrieve open ports information}"

# Top memory-consuming processes
mem_procs=$(ps -eo pid,comm,%mem --sort=-%mem 2>/dev/null | head -n 6 | awk 'NR>1 {print $1 " " $2 " " $3}' | paste -sd ";" -)
echo "topMemoryProcesses=${mem_procs:-Couldn't retrieve top memory processes information}"

# Top CPU-consuming processes
cpu_procs=$(ps -eo pid,comm,%cpu --sort=-%cpu 2>/dev/null | head -n 6 | awk 'NR>1 {print $1 " " $2 " " $3}' | paste -sd ";" -)
echo "topCpuProcesses=${cpu_procs:-Couldn't retrieve top CPU processes information}"

# Running services
running_services=$(systemctl list-units --type=service --state=running 2>/dev/null | awk 'NR>1 {print $1}' | paste -sd ',' - || \
                   service --status-all 2>/dev/null | grep '+' | awk '{print $4}' | paste -sd ',' -)
echo "runningServices=${running_services:-Couldn't retrieve running services information}"

# File systems
file_systems=$(df -hT 2>/dev/null | awk 'NR>1 {print $1":"$2":"$3":"$6}' | paste -sd ';' -)
echo "fileSystems=${file_systems:-Couldn't retrieve filesystems information}"

# Inode usage
inode_usage=$(df -ih 2>/dev/null | awk 'NR>1 {print $1":"$5":"$6}' | paste -sd ';' -)
echo "inodeUsage=${inode_usage:-Couldn't retrieve inode usage information}"

# Logged-in users
logged_users=$(who 2>/dev/null | awk '{print $1}' | sort | uniq | paste -sd ',' - || \
               users 2>/dev/null | tr ' ' '\n' | sort | uniq | paste -sd ',' -)
echo "loggedUsers=${logged_users:-Couldn't retrieve logged users information}"

# User accounts count
user_accounts_count=$(cat /etc/passwd 2>/dev/null | wc -l || \
                     getent passwd 2>/dev/null | wc -l)
echo "userAccountsCount=${user_accounts_count:-Couldn't retrieve user accounts count information}"

# Last reboot
last_reboot=$(last reboot 2>/dev/null | head -n 1 || \
              who -b 2>/dev/null | awk '{print $3, $4}')
echo "lastReboot=${last_reboot:-Couldn't retrieve last reboot information}"