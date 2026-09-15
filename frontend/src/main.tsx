import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { ProvedorAutenticacao } from "./context/ContextoAutenticacao";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <BrowserRouter>
      <ProvedorAutenticacao>
        <App />
      </ProvedorAutenticacao>
    </BrowserRouter>
  </React.StrictMode>,
);
