import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  //withCredentials: true, // Važno za session cookies
});

export default api;
