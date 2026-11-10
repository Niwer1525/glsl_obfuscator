//! Shared GLSL obfuscation engine.
//!
//! The crate root intentionally contains only module wiring and the public
//! context export. Language bindings use the functions in [`ffi`].

mod context;
mod ffi;
mod reserved;
mod transform;

pub use context::ObfuscationContext;
