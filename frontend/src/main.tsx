import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { ProvedorAutenticacao } from "./context/ContextoAutenticacao";
import { ProvedorDados } from "./context/ContextoDados";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <BrowserRouter>
      <ProvedorAutenticacao>
        <ProvedorDados>
          <App />
        </ProvedorDados>
      </ProvedorAutenticacao>
    </BrowserRouter>
  </React.StrictMode>,
);
